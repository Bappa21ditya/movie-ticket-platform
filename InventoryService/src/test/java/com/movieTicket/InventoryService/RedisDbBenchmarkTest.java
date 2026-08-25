package com.movieTicket.InventoryService;
import com.movieTicket.InventoryService.repos.ShowSeatRepository;
import com.movieTicket.InventoryService.services.ShowSeatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RedisDbBenchmarkTest {
    @Autowired
    private ShowSeatService showSeatService;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    private static final Long SHOW_SEAT_ID = 1L;

    private static final int TOTAL_REQUESTS = 10_000;

    private static final int THREAD_POOL_SIZE = 100;


    @Test
    void redisPlusDbBenchmark() throws Exception {

        // ========================================
        // 1. Reset seat
        // ========================================

        showSeatRepository.resetSeat(SHOW_SEAT_ID);


        // ========================================
        // 2. Create thread pool
        // ========================================

        ExecutorService executor =
                Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<Result>> futures =
                new ArrayList<>(TOTAL_REQUESTS);


        // ========================================
        // 3. Submit all requests
        // ========================================

        for (int i = 0; i < TOTAL_REQUESTS; i++) {

            futures.add(
                    executor.submit(() -> {

                        // Wait until all tasks are ready
                        startLatch.await();

                        long start =
                                System.nanoTime();

                        boolean success =
                                showSeatService
                                        .holdSeat(SHOW_SEAT_ID);

                        long end =
                                System.nanoTime();

                        double latencyMs =
                                (end - start)
                                        / 1_000_000.0;

                        return new Result(
                                success,
                                latencyMs
                        );
                    })
            );
        }


        // ========================================
        // 4. Start benchmark
        // ========================================

        long benchmarkStart =
                System.nanoTime();

        startLatch.countDown();


        // ========================================
        // 5. Collect results
        // ========================================

        int successful = 0;
        int rejected = 0;

        List<Double> latencies =
                new ArrayList<>(TOTAL_REQUESTS);

        for (Future<Result> future : futures) {

            Result result =
                    future.get();

            if (result.success()) {
                successful++;
            } else {
                rejected++;
            }

            latencies.add(
                    result.latencyMs()
            );
        }


        // ========================================
        // 6. Benchmark finished
        // ========================================

        long benchmarkEnd =
                System.nanoTime();

        executor.shutdown();

        executor.awaitTermination(
                30,
                TimeUnit.SECONDS
        );


        // ========================================
        // 7. Calculate statistics
        // ========================================

        Collections.sort(latencies);

        double p50 =
                percentile(
                        latencies,
                        50
                );

        double p95 =
                percentile(
                        latencies,
                        95
                );

        double p99 =
                percentile(
                        latencies,
                        99
                );

        double durationSeconds =
                (benchmarkEnd - benchmarkStart)
                        / 1_000_000_000.0;

        double throughput =
                TOTAL_REQUESTS
                        / durationSeconds;


        // ========================================
        // 8. Print results
        // ========================================

        System.out.println();

        System.out.println(
                "========== REDIS + DB BENCHMARK =========="
        );

        System.out.println(
                "Total Requests : " + TOTAL_REQUESTS
        );

        System.out.println(
                "Successful     : " + successful
        );

        System.out.println(
                "Rejected       : " + rejected
        );

        System.out.printf(
                "P50 Latency    : %.3f ms%n",
                p50
        );

        System.out.printf(
                "P95 Latency    : %.3f ms%n",
                p95
        );

        System.out.printf(
                "P99 Latency    : %.3f ms%n",
                p99
        );

        System.out.printf(
                "Duration       : %.3f sec%n",
                durationSeconds
        );

        System.out.printf(
                "Throughput     : %.0f req/sec%n",
                throughput
        );

        System.out.println(
                "=========================================="
        );


        // ========================================
        // 9. Correctness assertions
        // ========================================

        assertEquals(
                1,
                successful,
                "Exactly one request must succeed"
        );

        assertEquals(
                TOTAL_REQUESTS - 1,
                rejected,
                "All other requests must fail"
        );
    }


    // ========================================
    // Percentile calculation
    // ========================================

    private double percentile(
            List<Double> values,
            double percentile
    ) {

        int index =
                (int) Math.ceil(
                        percentile / 100.0
                                * values.size()
                ) - 1;

        return values.get(
                Math.max(0, index)
        );
    }


    // ========================================
    // Result
    // ========================================

    private record Result(
            boolean success,
            double latencyMs
    ) {
    }
}
