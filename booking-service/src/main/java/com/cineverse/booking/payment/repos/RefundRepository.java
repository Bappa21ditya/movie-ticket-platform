package com.cineverse.booking.payment.repos;

import com.cineverse.booking.payment.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, UUID> {

    Optional<Refund> findByPaymentId(UUID paymentId);

    Optional<Refund> findByBookingId(UUID bookingId);
}
