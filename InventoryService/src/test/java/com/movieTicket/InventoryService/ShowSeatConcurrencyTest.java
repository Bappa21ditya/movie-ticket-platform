//package com.movieTicket.InventoryService;
//
//import com.movieTicket.InventoryService.serviceImpl.ShowSeatServiceImpl;
//import com.movieTicket.InventoryService.services.ShowSeatService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//
//@SpringBootTest
//class ShowSeatConcurrencyTest {
//
//    @Autowired
//    private ShowSeatServiceImpl showSeatService;
//
//    @Test
//    void shouldDemonstrateOptimisticLocking() throws Exception {
//
//        CountDownLatch startLatch = new CountDownLatch(1);
//
//        ExecutorService executor = Executors.newFixedThreadPool(2);
//
//        Future<?> userA = executor.submit(() -> {
//            try {
//                startLatch.await();
//
//                showSeatService.holdSeatTest(1L);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//
//        Future<?> userB = executor.submit(() -> {
//            try {
//                startLatch.await();
//
//                showSeatService.holdSeatTest(1L);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//
//        // Start both threads
//        startLatch.countDown();
//
//        userA.get();
//        userB.get();
//
//        executor.shutdown();
//    }
//}
