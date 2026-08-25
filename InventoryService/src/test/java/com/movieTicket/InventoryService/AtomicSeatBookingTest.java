package com.movieTicket.InventoryService;
import com.movieTicket.InventoryService.services.ShowSeatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AtomicSeatBookingTest {

    @Autowired
    private ShowSeatService showSeatService;


    // ================================================================
    // TEST 1
    // ================================================================
    //
    // Purpose:
    // Verify that two requests trying to hold the SAME seat
    // concurrently result in only ONE successful hold.
    //
    // This is an atomic UPDATE test, NOT an optimistic locking test.
    //

    @Test
    void shouldAllowOnlyOneUserForSameSeat()
            throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CyclicBarrier barrier =
                new CyclicBarrier(2);

        AtomicInteger successCount =
                new AtomicInteger();

        Callable<Void> userA = () -> {

            barrier.await();

            boolean success =
                    showSeatService.holdSeat(1L);

            if (success) {
                successCount.incrementAndGet();
            }

            return null;
        };


        Callable<Void> userB = () -> {

            barrier.await();

            boolean success =
                    showSeatService.holdSeat(1L);

            if (success) {
                successCount.incrementAndGet();
            }

            return null;
        };


        Future<Void> resultA =
                executor.submit(userA);

        Future<Void> resultB =
                executor.submit(userB);


        resultA.get();
        resultB.get();


        executor.shutdown();


        System.out.println(
                "Successful holds = "
                        + successCount.get()
        );


        // Exactly ONE request must succeed.

        assertThat(successCount.get())
                .isEqualTo(1);
    }


    // ================================================================
    // TEST 2
    // ================================================================
    //
    // Purpose:
    // Prove correctness under 10,000 concurrent requests.
    //
    // No performance measurement here.
    //
    // We only care about:
    //
    // 10,000 requests
    //       ↓
    // same seat
    //       ↓
    // exactly 1 success
    //       ↓
    // 9,999 rejected
    //

    @Test
    void shouldAllowOnlyOneUserFor10000ConcurrentRequests()
            throws Exception {

        int userCount = 10_000;


        // ------------------------------------------------------------
        // Executor
        // ------------------------------------------------------------
        //
        // We don't create 10,000 Java threads.
        //
        // 10,000 requests
        //        ↓
        // 100 worker threads
        //        ↓
        // PostgreSQL
        //

        ExecutorService executor =
                Executors.newFixedThreadPool(100);


        // ------------------------------------------------------------
        // START LATCH
        // ------------------------------------------------------------

        CountDownLatch start =
                new CountDownLatch(1);


        // ------------------------------------------------------------
        // DONE LATCH
        // ------------------------------------------------------------

        CountDownLatch done =
                new CountDownLatch(userCount);


        // ------------------------------------------------------------
        // SUCCESS COUNTER
        // ------------------------------------------------------------

        AtomicInteger successCount =
                new AtomicInteger();


        // ------------------------------------------------------------
        // REJECTED COUNTER
        // ------------------------------------------------------------

        AtomicInteger rejectedCount =
                new AtomicInteger();


        // ------------------------------------------------------------
        // SUBMIT REQUESTS
        // ------------------------------------------------------------

        for (int i = 0; i < userCount; i++) {

            executor.submit(() -> {

                try {

                    // Wait until all requests are ready.

                    start.await();


                    // Every request tries the SAME seat.

                    boolean success =
                            showSeatService.holdSeat(1L);


                    if (success) {

                        successCount.incrementAndGet();

                    } else {

                        rejectedCount.incrementAndGet();
                    }


                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                } finally {

                    done.countDown();
                }
            });
        }


        // ------------------------------------------------------------
        // START ALL REQUESTS
        // ------------------------------------------------------------

        start.countDown();


        // ------------------------------------------------------------
        // WAIT FOR ALL REQUESTS
        // ------------------------------------------------------------

        done.await();


        executor.shutdown();


        // ------------------------------------------------------------
        // PRINT RESULT
        // ------------------------------------------------------------

        System.out.println();
        System.out.println(
                "========== 10,000 CONCURRENCY =========="
        );

        System.out.println(
                "Total Requests : "
                        + userCount
        );

        System.out.println(
                "Successful     : "
                        + successCount.get()
        );

        System.out.println(
                "Rejected       : "
                        + rejectedCount.get()
        );

        System.out.println(
                "========================================="
        );


        // ------------------------------------------------------------
        // CORRECTNESS
        // ------------------------------------------------------------

        assertThat(successCount.get())
                .isEqualTo(1);

        assertThat(rejectedCount.get())
                .isEqualTo(userCount - 1);
    }


    // ================================================================
    // TEST 3 — DB ONLY BENCHMARK
    // ================================================================
    //
    // Purpose:
    //
    // This test measures performance of the CURRENT DB-only strategy.
    //
    // We measure:
    //
    // - Successful requests
    // - Rejected requests
    // - P50
    // - P95
    // - P99
    // - Total duration
    // - Throughput
    //
    // This becomes the BASELINE before Redis.
    //

    @Test
    void benchmarkDbOnlyConcurrency()
            throws Exception {

        int userCount = 10_000;


        // ------------------------------------------------------------
        // EXECUTOR
        // ------------------------------------------------------------

        ExecutorService executor =
                Executors.newFixedThreadPool(100);


        // ------------------------------------------------------------
        // START LATCH
        // ------------------------------------------------------------

        CountDownLatch start =
                new CountDownLatch(1);


        // ------------------------------------------------------------
        // DONE LATCH
        // ------------------------------------------------------------

        CountDownLatch done =
                new CountDownLatch(userCount);


        // ------------------------------------------------------------
        // RESULT COUNTERS
        // ------------------------------------------------------------

        AtomicInteger successCount =
                new AtomicInteger();

        AtomicInteger rejectedCount =
                new AtomicInteger();


        // ------------------------------------------------------------
        // LATENCY STORAGE
        // ------------------------------------------------------------
        //
        // Stores the latency of every request.
        //

        List<Long> latencies =
                Collections.synchronizedList(
                        new ArrayList<>()
                );


        // ------------------------------------------------------------
        // SUBMIT REQUESTS
        // ------------------------------------------------------------

        for (int i = 0; i < userCount; i++) {

            executor.submit(() -> {

                try {

                    // Wait for benchmark start.

                    start.await();


                    // Start request timer.

                    long requestStart =
                            System.nanoTime();


                    // ------------------------------------------------
                    // ACTUAL DATABASE OPERATION
                    // ------------------------------------------------

                    boolean success =
                            showSeatService.holdSeat(1L);


                    // End request timer.

                    long requestEnd =
                            System.nanoTime();


                    // Store request latency.

                    latencies.add(
                            requestEnd - requestStart
                    );


                    // Record result.

                    if (success) {

                        successCount.incrementAndGet();

                    } else {

                        rejectedCount.incrementAndGet();
                    }


                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                } finally {

                    done.countDown();
                }
            });
        }


        // ------------------------------------------------------------
        // START BENCHMARK
        // ------------------------------------------------------------

        long testStart =
                System.nanoTime();

        start.countDown();


        // ------------------------------------------------------------
        // WAIT FOR ALL REQUESTS
        // ------------------------------------------------------------

        done.await();


        long testEnd =
                System.nanoTime();


        // ------------------------------------------------------------
        // SHUTDOWN
        // ------------------------------------------------------------

        executor.shutdown();

        executor.awaitTermination(
                30,
                TimeUnit.SECONDS
        );


        // ------------------------------------------------------------
        // SORT LATENCIES
        // ------------------------------------------------------------

        List<Long> sortedLatencies =
                new ArrayList<>(latencies);

        Collections.sort(sortedLatencies);


        // ------------------------------------------------------------
        // P50
        // ------------------------------------------------------------

        long p50 =
                percentile(
                        sortedLatencies,
                        50
                );


        // ------------------------------------------------------------
        // P95
        // ------------------------------------------------------------

        long p95 =
                percentile(
                        sortedLatencies,
                        95
                );


        // ------------------------------------------------------------
        // P99
        // ------------------------------------------------------------

        long p99 =
                percentile(
                        sortedLatencies,
                        99
                );


        // Convert nanoseconds → milliseconds.

        double p50Ms =
                p50 / 1_000_000.0;

        double p95Ms =
                p95 / 1_000_000.0;

        double p99Ms =
                p99 / 1_000_000.0;


        // ------------------------------------------------------------
        // TOTAL DURATION
        // ------------------------------------------------------------

        double durationSeconds =
                (testEnd - testStart)
                        / 1_000_000_000.0;


        // ------------------------------------------------------------
        // THROUGHPUT
        // ------------------------------------------------------------

        double throughput =
                userCount / durationSeconds;


        // ------------------------------------------------------------
        // PRINT RESULTS
        // ------------------------------------------------------------

        System.out.println();

        System.out.println(
                "========== DB ONLY BENCHMARK =========="
        );

        System.out.println(
                "Total Requests : "
                        + userCount
        );

        System.out.println(
                "Successful     : "
                        + successCount.get()
        );

        System.out.println(
                "Rejected       : "
                        + rejectedCount.get()
        );

        System.out.println(
                "P50 Latency    : "
                        + p50Ms
                        + " ms"
        );

        System.out.println(
                "P95 Latency    : "
                        + p95Ms
                        + " ms"
        );

        System.out.println(
                "P99 Latency    : "
                        + p99Ms
                        + " ms"
        );

        System.out.println(
                "Duration       : "
                        + durationSeconds
                        + " sec"
        );

        System.out.println(
                "Throughput     : "
                        + throughput
                        + " req/sec"
        );

        System.out.println(
                "========================================"
        );


        // ------------------------------------------------------------
        // CORRECTNESS CHECKS
        // ------------------------------------------------------------

        assertThat(successCount.get())
                .isEqualTo(1);

        assertThat(rejectedCount.get())
                .isEqualTo(userCount - 1);

        assertThat(
                successCount.get()
                        + rejectedCount.get()
        ).isEqualTo(userCount);

        assertThat(latencies)
                .hasSize(userCount);
    }


    // ================================================================
    // PERCENTILE HELPER
    // ================================================================

    private long percentile(
            List<Long> values,
            double percentile) {

        int index =
                (int) Math.ceil(
                        percentile
                                / 100.0
                                * values.size()
                ) - 1;

        return values.get(
                Math.max(index, 0)
        );
    }
}