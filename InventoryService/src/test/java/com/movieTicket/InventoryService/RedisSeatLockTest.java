package com.movieTicket.InventoryService;

import com.movieTicket.InventoryService.reddis.SeatLock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RedisSeatLockTest {

    @Autowired
    private SeatLock seatLock;

    @Test
    void shouldAllowOnlyOneRequestForSameSeat() {

        Long showSeatId = 1L;

        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();

        boolean first =
                seatLock.tryLock(showSeatId, token1);

        boolean second =
                seatLock.tryLock(showSeatId, token2);

        System.out.println("First  = " + first);
        System.out.println("Second = " + second);

        seatLock.unlock(showSeatId, token1);
    }

    @Test
    void shouldAllowAnotherRequestAfterUnlock() {

        Long showSeatId = 1L;

        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();

        boolean first =
                seatLock.tryLock(showSeatId, token1);

        assertTrue(first);

        seatLock.unlock(showSeatId, token1);

        boolean second =
                seatLock.tryLock(showSeatId, token2);

        assertTrue(second);

        seatLock.unlock(showSeatId, token2);

        System.out.println("First lock  = " + first);
        System.out.println("Second lock = " + second);
    }

    @Test
    void shouldAllowOnlyOneUserForSameSeat() throws InterruptedException, ExecutionException {

        Long showSeatId = 1L;

        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Boolean> request1 = () ->
                seatLock.tryLock(showSeatId, token1);

        Callable<Boolean> request2 = () ->
                seatLock.tryLock(showSeatId, token2);

        Future<Boolean> result1 = executor.submit(request1);
        Future<Boolean> result2 = executor.submit(request2);

        boolean first = result1.get();
        boolean second = result2.get();

        System.out.println("Request 1 = " + first);
        System.out.println("Request 2 = " + second);

        assertTrue(first ^ second);

        if (first) {
            seatLock.unlock(showSeatId, token1);
        }

        if (second) {
            seatLock.unlock(showSeatId, token2);
        }

        executor.shutdown();
    }
}
