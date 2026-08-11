package com.movieTicket.InventoryService;

import com.movieTicket.InventoryService.services.ShowSeatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class AtomicSeatBookingTest {

    @Autowired
    private ShowSeatService showSeatService;

    private final CyclicBarrier barrier =
            new CyclicBarrier(2);

    @Test
    void testOptimisticLocking() throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        Callable<Void> userA = () -> {
            // call your test-specific method if needed
            barrier.await();

            return null;
        };

        Callable<Void> userB = () -> {
            barrier.await();

            return null;
        };

        Future<Void> resultA = executor.submit(userA);
        Future<Void> resultB = executor.submit(userB);

        resultA.get();
        resultB.get();

        executor.shutdown();
    }

    @Test
    void shouldAllowOnlyOneUserFor10000ConcurrentRequests()
            throws Exception {

        // Simulate 10,000 concurrent booking requests.
        int userCount = 10_000;


        // IMPORTANT:
        //
        // We DON'T create 10,000 Java threads.
        //
        // Only 100 worker threads execute at the same time.
        // The remaining tasks wait in the executor queue.
        //
        // This gives us:
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
        //
        // All submitted tasks wait here initially.
        //
        // When countDown() is called, queued/running tasks
        // are allowed to proceed.
        //
        CountDownLatch start =
                new CountDownLatch(1);


        // ------------------------------------------------------------
        // DONE LATCH
        // ------------------------------------------------------------
        //
        // We have 10,000 requests, so we need to wait
        // for all 10,000 tasks to finish.
        //
        CountDownLatch done =
                new CountDownLatch(userCount);


        // Number of successful seat acquisitions.
        AtomicInteger successCount =
                new AtomicInteger();


        // Submit 10,000 booking requests.
        for (int i = 0; i < userCount; i++) {

            executor.submit(() -> {

                try {

                    // Wait until the main thread releases
                    // the test.
                    start.await();


                    // Every request attempts to hold
                    // the SAME ShowSeat.
                    //
                    // Atomic UPDATE:
                    //
                    // UPDATE show_seat
                    // SET status = 'HELD'
                    // WHERE id = 1
                    // AND status = 'AVAILABLE';
                    //
                    boolean success =
                            showSeatService.holdSeat(1L);


                    // Only the request that successfully
                    // changes AVAILABLE → HELD returns true.
                    //
                    // All other requests receive:
                    //
                    // updatedRows = 0
                    //
                    if (success) {
                        successCount.incrementAndGet();
                    }


                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                } finally {

                    // Mark this request as completed.
                    done.countDown();
                }
            });
        }


        // ------------------------------------------------------------
        // START ALL REQUESTS
        // ------------------------------------------------------------
        //
        // Release the waiting tasks.
        //
        // The executor will process them using its
        // 100 worker threads.
        //
        start.countDown();


        // ------------------------------------------------------------
        // WAIT FOR ALL 10,000 REQUESTS
        // ------------------------------------------------------------
        //
        // The test continues only after every submitted
        // request has completed.
        //
        done.await();


        executor.shutdown();


        System.out.println(
                "Successful holds = "
                        + successCount.get()
        );


        // ------------------------------------------------------------
        // BUSINESS INVARIANT
        // ------------------------------------------------------------
        //
        // Even with 10,000 requests:
        //
        // SUCCESS must always be exactly 1.
        //
        // This proves that our atomic state transition
        // prevents double booking.
        //
        assertThat(successCount.get())
                .isEqualTo(1);
    }

}
