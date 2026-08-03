package com.movieTicket.InventoryService.repos;

import com.movieTicket.InventoryService.entity.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold,Long> {
    List<SeatHold> findByBookingId(Long bookingId);

    List<SeatHold> findByUserId(Long userId);
}
