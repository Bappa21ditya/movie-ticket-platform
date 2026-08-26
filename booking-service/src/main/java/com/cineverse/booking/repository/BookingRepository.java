package com.cineverse.booking.repository;

import com.cineverse.booking.entity.Booking;
import com.cineverse.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUserId(UUID userId);

    List<Booking> findByShowId(UUID showId);

    List<Booking> findByStatus(BookingStatus status);
}
