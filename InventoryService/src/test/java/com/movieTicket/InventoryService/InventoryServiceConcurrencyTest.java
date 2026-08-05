package com.movieTicket.InventoryService;

import com.movieTicket.InventoryService.dtos.CreateSeatHoldRequest;
import com.movieTicket.InventoryService.dtos.SeatHoldResponse;
import com.movieTicket.InventoryService.exceptions.SeatUnavailableException;
import com.movieTicket.InventoryService.services.SeatHoldService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.movieTicket.InventoryService.dtos.CreateSeatRequest;
import com.movieTicket.InventoryService.services.SeatHoldService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class InventoryServiceConcurrencyTest {

    @Autowired
    private SeatHoldService seatHoldService;

    @Test
    void shouldTestConcurrentSeatHoldForTwoUsers() throws Exception {

        // Both users try to hold the SAME ShowSeat
        CreateSeatHoldRequest requestA = new CreateSeatHoldRequest();

        requestA.setShowSeatId(1L);
        requestA.setBookingId(1001L);
        requestA.setUserId(101L);
        requestA.setExpiresAt(LocalDateTime.now().plusMinutes(5));


        CreateSeatHoldRequest requestB = new CreateSeatHoldRequest();

        requestB.setShowSeatId(1L);
        requestB.setBookingId(1002L);
        requestB.setUserId(102L);
        requestB.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        // Keeps both threads waiting until we release them
        CountDownLatch startLatch =
                new CountDownLatch(1);

        // Prevents the test from finishing before both threads complete
        CountDownLatch finishLatch =
                new CountDownLatch(2);

        Future<SeatHoldResponse> userA = executor.submit(() -> {

            try {
                startLatch.await();

                return seatHoldService.createHold(requestA);

            } finally {
                finishLatch.countDown();
            }
        });

        Future<SeatHoldResponse> userB = executor.submit(() -> {

            try {
                startLatch.await();

                return seatHoldService.createHold(requestB);

            } finally {
                finishLatch.countDown();
            }
        });

        // Release both threads
        System.out.println("Starting concurrent requests...");

        startLatch.countDown();

        // Wait until both requests finish
        finishLatch.await();

        // Get results
        SeatHoldResponse responseA = null;
        SeatHoldResponse responseB = null;

        boolean userAFailed = false;
        boolean userBFailed = false;

        try {
            responseA = userA.get();
        } catch (ExecutionException e) {
            e.getCause().printStackTrace();

            if (e.getCause() instanceof SeatUnavailableException) {
                userAFailed = true;
            } else {
                throw e;
            }
        }
        try {
            responseB = userB.get();
        } catch (ExecutionException e) {
            e.getCause().printStackTrace();

            if (e.getCause() instanceof SeatUnavailableException) {
                userBFailed = true;
            } else {
                throw e;
            }
        }

        System.out.println("User A result = " + responseA);
        System.out.println("User B result = " + responseB);


        assertEquals(
                1,
                (responseA != null ? 1 : 0)
                        + (responseB != null ? 1 : 0)
        );

        assertEquals(
                1,
                (userAFailed ? 1 : 0)
                        + (userBFailed ? 1 : 0)
        );
    }

    // for 100 threads
    @Test
    void shouldTestConcurrentSeatHoldFor1000Users() throws Exception {

        int numberOfUsers = 1000;

        long startTime = System.currentTimeMillis();

        ExecutorService executor =
                Executors.newFixedThreadPool(numberOfUsers);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch finishLatch =
                new CountDownLatch(numberOfUsers);

        List<Future<SeatHoldResponse>> futures =
                new ArrayList<>();

        // Create 1000 concurrent requests
        for (int i = 0; i < numberOfUsers; i++) {

            long bookingId = 1000L + i;
            long userId = 100L + i;

            CreateSeatHoldRequest request =
                    new CreateSeatHoldRequest();

            // ALL users try to hold SAME ShowSeat
            request.setShowSeatId(1L);

            request.setBookingId(bookingId);
            request.setUserId(userId);
            request.setExpiresAt(
                    LocalDateTime.now().plusMinutes(5)
            );

            Future<SeatHoldResponse> future =
                    executor.submit(() -> {

                        try {

                            // Wait until all threads are ready
                            startLatch.await();

                            return seatHoldService.createHold(request);

                        } finally {

                            finishLatch.countDown();
                        }
                    });

            futures.add(future);
        }

        System.out.println(
                "Starting " + numberOfUsers +
                        " concurrent requests..."
        );

        // Release all threads
        startLatch.countDown();

        // Wait for all requests to finish
        finishLatch.await();

        int successCount = 0;
        int failureCount = 0;

        for (Future<SeatHoldResponse> future : futures) {

            try {

                SeatHoldResponse response = future.get();

                if (response != null) {
                    successCount++;
                }

            } catch (ExecutionException e) {

                if (isExpectedConcurrencyFailure(e.getCause())) {

                    failureCount++;

                } else {

                    e.getCause().printStackTrace();

                    throw e;
                }
            }
        }

        executor.shutdown();

        long endTime = System.currentTimeMillis();

        System.out.println("Execution time : "
                + (endTime - startTime) + " ms");

        System.out.println("------------------------------");
        System.out.println("Total requests : " + numberOfUsers);
        System.out.println("Successful     : " + successCount);
        System.out.println("Failed         : " + failureCount);
        System.out.println("------------------------------");

        assertEquals(
                1,
                successCount,
                "Exactly one request should successfully hold the seat"
        );

        assertEquals(
                numberOfUsers - 1,
                failureCount,
                "All other requests should fail"
        );
    }


    @Test
void shouldTestConcurrentSeatHoldFor10000Users() throws Exception {

    int numberOfUsers = 10000;

    long startTime = System.currentTimeMillis();

    ExecutorService executor =
            Executors.newFixedThreadPool(100);

    CountDownLatch startLatch =
            new CountDownLatch(1);

    CountDownLatch finishLatch =
            new CountDownLatch(numberOfUsers);

    List<Future<SeatHoldResponse>> futures =
            new ArrayList<>();

    // Create 1000 concurrent requests
    for (int i = 0; i < numberOfUsers; i++) {

        long bookingId = 1000L + i;
        long userId = 100L + i;

        CreateSeatHoldRequest request =
                new CreateSeatHoldRequest();

        // ALL users try to hold SAME ShowSeat
        request.setShowSeatId(1L);

        request.setBookingId(bookingId);
        request.setUserId(userId);
        request.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );

        Future<SeatHoldResponse> future =
                executor.submit(() -> {

                    try {

                        // Wait until all threads are ready
                        startLatch.await();

                        return seatHoldService.createHold(request);

                    } finally {

                        finishLatch.countDown();
                    }
                });

        futures.add(future);
    }

    System.out.println(
            "Starting " + numberOfUsers +
                    " concurrent requests..."
    );

    // Release all threads
    startLatch.countDown();

    // Wait for all requests to finish
    finishLatch.await();

    int successCount = 0;
    int failureCount = 0;

    for (Future<SeatHoldResponse> future : futures) {

        try {

            SeatHoldResponse response = future.get();

            if (response != null) {
                successCount++;
            }

        } catch (ExecutionException e) {

            if (isExpectedConcurrencyFailure(e.getCause())) {

                failureCount++;

            } else {

                e.getCause().printStackTrace();

                throw e;
            }
        }
    }

    executor.shutdown();

    long endTime = System.currentTimeMillis();

    System.out.println("Execution time : "
            + (endTime - startTime) + " ms");

    System.out.println("------------------------------");
    System.out.println("Total requests : " + numberOfUsers);
    System.out.println("Successful     : " + successCount);
    System.out.println("Failed         : " + failureCount);
    System.out.println("------------------------------");

    assertEquals(
            1,
            successCount,
            "Exactly one request should successfully hold the seat"
    );

    assertEquals(
            numberOfUsers - 1,
            failureCount,
            "All other requests should fail"
    );
}
    private boolean isExpectedConcurrencyFailure(Throwable throwable) {

        Throwable current = throwable;

        while (current != null) {

            if (current instanceof SeatUnavailableException ||
                    current instanceof ObjectOptimisticLockingFailureException) {

                return true;
            }

            current = current.getCause();
        }

        return false;
    }


}