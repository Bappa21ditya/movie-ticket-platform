package com.cineverse.booking.payment.repos;

import com.cineverse.booking.payment.entity.PaymentTransaction;
import org.springframework.stereotype.Repository;
import com.cineverse.booking.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository  extends JpaRepository<PaymentTransaction, UUID> {

    List<PaymentTransaction> findByPaymentId(UUID paymentId);
}
