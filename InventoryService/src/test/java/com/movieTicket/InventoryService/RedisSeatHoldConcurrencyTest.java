package com.movieTicket.InventoryService;

import com.movieTicket.InventoryService.repos.ShowSeatRepository;
import com.movieTicket.InventoryService.services.SeatService;
import com.movieTicket.InventoryService.services.ShowSeatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RedisSeatHoldConcurrencyTest {

    @Autowired
    private ShowSeatService showSeatService;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Test
    void shouldAllowOnlyOneConcurrentHold() throws Exception {

        Long showSeatId = 1L;



        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> request = () -> {
            start.await();

            try {
                return showSeatService.holdSeat(showSeatId);
            } catch (Exception e) {
                return false;
            }
        };

        Future<Boolean> first = executor.submit(request);
        Future<Boolean> second = executor.submit(request);

        // Release both requests at approximately the same time
        start.countDown();

        boolean result1 = first.get();
        boolean result2 = second.get();

        executor.shutdown();

        System.out.println("Request 1 = " + result1);
        System.out.println("Request 2 = " + result2);

        int successCount = 0;

        if (result1) successCount++;
        if (result2) successCount++;

        assertEquals(1, successCount);
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 100, 1000, 10000})
    void shouldAllowOnlyOneConcurrentHold(int requestCount)
            throws Exception {

        Long showSeatId = 1L;

        // IMPORTANT: reset the seat before every test
        showSeatRepository.resetSeat(showSeatId);

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        Math.min(requestCount, 100)
                );

        CountDownLatch start = new CountDownLatch(1);

        List<Callable<Boolean>> requests = new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {

            requests.add(() -> {
                start.await();

                return showSeatService.holdSeat(showSeatId);
            });
        }

        List<Future<Boolean>> futures = new ArrayList<>();

        // Submit all requests first
        for (Callable<Boolean> request : requests) {
            futures.add(executor.submit(request));
        }

        // Release everybody at approximately the same time
        start.countDown();

        int successCount = 0;
        int failureCount = 0;

        for (Future<Boolean> future : futures) {

            boolean success = future.get();

            if (success) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println(
                "Requests  : " + requestCount
        );
        System.out.println(
                "Successful: " + successCount
        );
        System.out.println(
                "Failed    : " + failureCount
        );

        assertEquals(
                1,
                successCount,
                "Exactly one request must successfully hold the seat"
        );

        assertEquals(
                requestCount - 1,
                failureCount
        );
    }

}
