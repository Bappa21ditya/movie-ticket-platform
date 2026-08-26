package com.cineverse.booking.repository;

import com.cineverse.booking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByBookingBookingId(UUID bookingId);

    Optional<Ticket> findByTicketNumber(String ticketNumber);
}
