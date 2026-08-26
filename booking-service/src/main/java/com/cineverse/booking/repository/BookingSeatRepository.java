package com.cineverse.booking.repository;

import com.cineverse.booking.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, UUID> {



        List<BookingSeat> findByBookingBookingId(UUID bookingId);

        boolean existsByBookingBookingIdAndSeatId(
                UUID bookingId,
                UUID seatId
    );
}
