package com.cineverse.booking.payment.service;

import com.cineverse.booking.payment.dto.CreatePaymentRequest;
import com.cineverse.booking.payment.dto.PaymentResponse;
import com.cineverse.booking.payment.dto.RefundResponse;
import com.cineverse.booking.payment.entity.Payment;
import com.cineverse.booking.payment.entity.PaymentTransaction;
import com.cineverse.booking.payment.entity.Refund;
import com.cineverse.booking.payment.enums.PaymentMethod;
import com.cineverse.booking.payment.enums.PaymentStatus;
import com.cineverse.booking.payment.enums.PaymentTransactionStatus;
import com.cineverse.booking.payment.enums.RefundStatus;
import com.cineverse.booking.payment.repos.PaymentRepository;
import com.cineverse.booking.payment.repos.PaymentTransactionRepository;
import com.cineverse.booking.payment.repos.RefundRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
//@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final RefundRepository refundRepository;

    @Override
    public PaymentResponse createPayment(
            CreatePaymentRequest request) {


        System.out.println("we are inside Create Payment....");
        Payment payment = paymentRepository
                .findByBookingId(request.getBookingId())
                .orElseGet(() -> {

                    Payment newPayment = Payment.builder()
                            .bookingId(request.getBookingId())
                            .amount(request.getAmount())
                            .paymentMethod(request.getPaymentMethod())
                            .status(PaymentStatus.PROCESSING)
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();


                    System.out.println("Payemt is working ....");
                    return paymentRepository.save(newPayment);
                });

        // TEST PAYMENT FAILURE
        if (request.getPaymentMethod() == PaymentMethod.UPI) {

            PaymentTransaction transaction =
                    PaymentTransaction.builder()
                            .paymentId(payment.getPaymentId())
                            .amount(request.getAmount())
                            .status(PaymentTransactionStatus.SUCCESS)
                            .gatewayTransactionId(
                                    UUID.randomUUID().toString()
                            )
                            .createdAt(OffsetDateTime.now())
                            .build();

            transactionRepository.save(transaction);

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setUpdatedAt(OffsetDateTime.now());

            paymentRepository.save(payment);

            return mapToResponse(payment);
        }


        PaymentTransaction transaction =
                PaymentTransaction.builder()
                        .paymentId(payment.getPaymentId())
                        .amount(request.getAmount())
                        .status(PaymentTransactionStatus.SUCCESS)
                        .gatewayTransactionId(
                                UUID.randomUUID().toString()
                        )
                        .createdAt(OffsetDateTime.now())
                        .build();

        transactionRepository.save(transaction);

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setUpdatedAt(OffsetDateTime.now());

        paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found")
                );

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBookingId(
            UUID bookingId) {

        Payment payment =
                paymentRepository.findByBookingId(bookingId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found"
                                )
                        );

        return mapToResponse(payment);
    }

    @Override
    @Transactional
    public RefundResponse refundPayment(UUID bookingId) {

        // 1. Find payment
        Payment payment = paymentRepository
                .findByBookingId(bookingId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Payment not found for booking: " + bookingId
                        )
                );

        // 2. Payment must have succeeded
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Cannot refund payment with status: "
                            + payment.getStatus()
            );
        }

        // 3. Idempotency check
        Optional<Refund> existingRefund =
                refundRepository.findByPaymentId(payment.getPaymentId());

        if (existingRefund.isPresent()) {

            Refund refund = existingRefund.get();

            return RefundResponse.builder()
                    .refundId(refund.getRefundId())
                    .bookingId(refund.getBookingId())
                    .amount(refund.getAmount())
                    .status(refund.getStatus())
                    .build();
        }

        // 4. Create refund
        Refund refund = Refund.builder()
                .paymentId(payment.getPaymentId())
                .bookingId(bookingId)
                .amount(payment.getAmount())
                .status(RefundStatus.PENDING)
                .reason("Booking compensation")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        refund = refundRepository.save(refund);

        // 5. Update payment status
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(OffsetDateTime.now());

        paymentRepository.save(payment);

        return RefundResponse.builder()
                .refundId(refund.getRefundId())
                .bookingId(bookingId)
                .amount(refund.getAmount())
                .status(refund.getStatus())
                .build();
    }

    private PaymentResponse mapToResponse(Payment payment) {

        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .bookingId(payment.getBookingId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public RefundResponse processPendingRefund(UUID bookingId) {

        Refund refund = refundRepository
                .findByBookingId(bookingId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Refund not found for booking: " + bookingId
                        )
                );

        // Already successful → idempotent response
        if (refund.getStatus() == RefundStatus.SUCCESS) {
            return RefundResponse.builder()
                    .refundId(refund.getRefundId())
                    .bookingId(refund.getBookingId())
                    .amount(refund.getAmount())
                    .status(refund.getStatus())
                    .build();
        }

        // ==========================================
        // TEST 4 - SIMULATE REFUND SUCCESS
        // ==========================================

        refund.setStatus(RefundStatus.SUCCESS);
        refund.setUpdatedAt(OffsetDateTime.now());

        refundRepository.save(refund);

        System.out.println("===== REFUND SUCCESS =====");
        System.out.println("Refund ID: " + refund.getRefundId());
        System.out.println("Booking ID: " + bookingId);
        System.out.println("Amount: " + refund.getAmount());

        return RefundResponse.builder()
                .refundId(refund.getRefundId())
                .bookingId(bookingId)
                .amount(refund.getAmount())
                .status(RefundStatus.SUCCESS)
                .build();
    }
}