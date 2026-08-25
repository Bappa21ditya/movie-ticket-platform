package com.movieTicket.InventoryService.reddis;

public interface SeatLock {


    boolean tryLock(Long showSeatId, String token);

    void unlock(Long showSeatId, String token);

    }
