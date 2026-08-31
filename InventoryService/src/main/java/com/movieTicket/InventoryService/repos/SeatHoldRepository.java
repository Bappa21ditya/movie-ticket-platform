package com.movieTicket.InventoryService.repos;

import com.movieTicket.InventoryService.entity.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold,Long> {
    List<SeatHold> findByBookingId(Long bookingId);

    List<SeatHold> findByUserId(Long userId);

    Optional<SeatHold> findByShowSeatIdAndBookingId(
            Long showSeatId,
            UUID bookingId
    );
}
