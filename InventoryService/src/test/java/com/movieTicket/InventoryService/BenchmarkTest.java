package com.movieTicket.InventoryService;

import com.movieTicket.InventoryService.services.SeatHoldService;
import com.movieTicket.InventoryService.services.ShowSeatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class BenchmarkTest {
    @Autowired
    private ShowSeatService showSeatService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final AtomicBoolean isMonitoring = new AtomicBoolean(false);

    // Target seat ID you want to contend for
    private static final String URL = "http://localhost:8081/api/v1/show-seats/1/hold";
    private static final int TOTAL_REQUESTS = 10_000;
    private static final int CONCURRENCY = 100;

    @Test
    void runContentionBenchmark() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        RestTemplate restTemplate = new RestTemplate();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        System.out.println("Firing 10,000 requests to hold seat 1 simultaneously...");
        Instant start = Instant.now();

        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            executor.submit(() -> {
                try {
                    restTemplate.postForEntity(URL, null, Boolean.class);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 409 Conflict / non-2xx responses go here
                    rejectedCount.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        Instant finish = Instant.now();
        double durationSeconds = Duration.between(start, finish).toMillis() / 1000.0;

        System.out.println("\n--- BENCHMARK RESULTS ---");
        System.out.println("Duration   : " + durationSeconds + " seconds");
        System.out.println("Throughput : " + (int)(TOTAL_REQUESTS / durationSeconds) + " req/sec");
        System.out.println("Successful : " + successCount.get() + " (Expect exactly 1)");
        System.out.println("Rejected   : " + rejectedCount.get() + " (Expect 9,999)");
    }
    @Test
    void testConcurrentHoldSeat() throws InterruptedException {
        int totalRequests = 10000;
        Long targetShowSeatId = 1L;

        // 1. Reset seat state
        jdbcTemplate.execute("UPDATE inventory_db.show_seats SET status = 'AVAILABLE' WHERE show_seat_id = " + targetShowSeatId);

        // 2. Setup concurrency controls
        ExecutorService executor = Executors.newFixedThreadPool(100); // 100 parallel worker threads
        CountDownLatch startLatch = new CountDownLatch(1);             // Fire all threads at the exact same instant
        CountDownLatch finishLatch = new CountDownLatch(totalRequests); // Block main test until all finish

        AtomicInteger successfulHolds = new AtomicInteger(0);
        AtomicInteger rejectedHolds = new AtomicInteger(0);

        System.out.println("Firing " + totalRequests + " requests to hold seat " + targetShowSeatId + " simultaneously...");

        // 3. Start high-frequency monitor daemon thread
        startPostgresMonitor();

        long startTime = System.nanoTime();

        // 4. Submit 10,000 tasks to executor
        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for trigger signal
                    boolean result = showSeatService.holdSeat(targetShowSeatId);
                    if (result) {
                        successfulHolds.incrementAndGet();
                    } else {
                        rejectedHolds.incrementAndGet();
                    }
                } catch (Exception e) {
                    rejectedHolds.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // 5. Release all threads simultaneously
        startLatch.countDown();

        // 6. Block main JUnit thread until all 10,000 requests complete
        finishLatch.await(30, TimeUnit.SECONDS);
        long endTime = System.nanoTime();

        // 7. Stop monitor and shutdown thread pool
        stopPostgresMonitor();
        executor.shutdown();

        // 8. Print Benchmark Summary
        double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
        double throughput = totalRequests / durationSeconds;

        System.out.println("\n--- BENCHMARK RESULTS ---");
        System.out.printf("Duration   : %.3f seconds%n", durationSeconds);
        System.out.printf("Throughput : %.0f req/sec%n", throughput);
        System.out.printf("Successful : %d%n", successfulHolds.get());
        System.out.printf("Rejected   : %d%n", rejectedHolds.get());
    }

    private void startPostgresMonitor() {
        isMonitoring.set(true);
        Thread monitorThread = new Thread(() -> {
            System.out.println("=== STARTING POSTGRES HIGH-FREQUENCY MONITOR ===");
            while (isMonitoring.get()) {
                try {
                    jdbcTemplate.query(
                            """
                            SELECT 
                              count(*) AS total_conn,
                              count(*) FILTER (WHERE state = 'active') AS active,
                              count(*) FILTER (WHERE state = 'idle') AS idle,
                              count(*) FILTER (WHERE wait_event_type IS NOT NULL AND state = 'active') AS waiting
                            FROM pg_stat_activity 
                            WHERE datname = current_database();
                            """,
                            rs -> {
                                int active = rs.getInt("active");
                                int waiting = rs.getInt("waiting");
                                if (active > 1 || waiting > 0) {
                                    System.out.printf(
                                            "[DB METRICS] Total: %d | Active: %d | Idle: %d | Lock Waiting: %d%n",
                                            rs.getInt("total_conn"), active, rs.getInt("idle"), waiting
                                    );
                                }
                            }
                    );
                    Thread.sleep(50);
                } catch (Exception ignored) {}
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void stopPostgresMonitor() {
        isMonitoring.set(false);
    }
}
