package com.movieTicket.InventoryService;
import com.movieTicket.InventoryService.repos.ShowSeatRepository;
import com.movieTicket.InventoryService.services.ShowSeatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisSeatHoldBenchmarkTest {
    @Autowired
    private ShowSeatService showSeatService;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    private static final Long SHOW_SEAT_ID = 1L;
    private static final int TOTAL_REQUESTS = 10_000;
    private static final int THREAD_POOL_SIZE = 100;

    @Test
    void redisPlusDbBenchmark() throws Exception {

        // ----------------------------------------
        // 1. Reset seat
        // ----------------------------------------

        showSeatRepository.resetSeat(SHOW_SEAT_ID);

        // ----------------------------------------
        // 2. Create thread pool
        // ----------------------------------------

        ExecutorService executor =
                Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<Result>> futures =
                new ArrayList<>(TOTAL_REQUESTS);

        // ----------------------------------------
        // 3. Submit requests
        // ----------------------------------------

        for (int i = 0; i < TOTAL_REQUESTS; i++) {

            futures.add(
                    executor.submit(() -> {

                        startLatch.await();

                        long start =
                                System.nanoTime();

                        boolean success =
                                showSeatService
                                        .holdSeat(SHOW_SEAT_ID);

                        long end =
                                System.nanoTime();

                        double latencyMs =
                                (end - start) / 1_000_000.0;

                        return new Result(
                                success,
                                latencyMs
                        );
                    })
            );
        }

        // ----------------------------------------
        // 4. Start all requests
        // ----------------------------------------

        long benchmarkStart =
                System.nanoTime();

        startLatch.countDown();

        // ----------------------------------------
        // 5. Collect results
        // ----------------------------------------

        int successful = 0;
        int rejected = 0;

        List<Double> latencies =
                new ArrayList<>(TOTAL_REQUESTS);

        for (Future<Result> future : futures) {

            Result result = future.get();

            if (result.success()) {
                successful++;
            } else {
                rejected++;
            }

            latencies.add(result.latencyMs());
        }

        long benchmarkEnd =
                System.nanoTime();

        executor.shutdown();

        executor.awaitTermination(
                30,
                TimeUnit.SECONDS
        );

        // ----------------------------------------
        // 6. Calculate statistics
        // ----------------------------------------

        Collections.sort(latencies);

        double p50 =
                percentile(latencies, 50);

        double p95 =
                percentile(latencies, 95);

        double p99 =
                percentile(latencies, 99);

        double durationSeconds =
                (benchmarkEnd - benchmarkStart)
                        / 1_000_000_000.0;

        double throughput =
                TOTAL_REQUESTS / durationSeconds;

        // ----------------------------------------
        // 7. Print results
        // ----------------------------------------

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

        System.out.println(
                "P50 Latency    : " + p50 + " ms"
        );

        System.out.println(
                "P95 Latency    : " + p95 + " ms"
        );

        System.out.println(
                "P99 Latency    : " + p99 + " ms"
        );

        System.out.println(
                "Duration       : " + durationSeconds + " sec"
        );

        System.out.println(
                "Throughput     : " + throughput + " req/sec"
        );

        System.out.println(
                "=========================================="
        );

        // ----------------------------------------
        // 8. Correctness assertions
        // ----------------------------------------

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

    private double percentile(
            List<Double> values,
            double percentile) {

        int index =
                (int) Math.ceil(
                        percentile / 100.0
                                * values.size()
                ) - 1;

        return values.get(
                Math.max(0, index)
        );
    }

    private record Result(
            boolean success,
            double latencyMs
    ) {
    }
}
