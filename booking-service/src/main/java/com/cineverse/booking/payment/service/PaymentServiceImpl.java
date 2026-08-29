package com.cineverse.booking.payment.service;

import com.cineverse.booking.payment.dto.CreatePaymentRequest;
import com.cineverse.booking.payment.dto.PaymentResponse;
import com.cineverse.booking.payment.entity.Payment;
import com.cineverse.booking.payment.entity.PaymentTransaction;
import com.cineverse.booking.payment.enums.PaymentMethod;
import com.cineverse.booking.payment.enums.PaymentStatus;
import com.cineverse.booking.payment.enums.PaymentTransactionStatus;
import com.cineverse.booking.payment.repos.PaymentRepository;
import com.cineverse.booking.payment.repos.PaymentTransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService{

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;

    @Override
    public PaymentResponse createPayment(
            CreatePaymentRequest request) {

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
    public PaymentResponse refundPayment(UUID paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found")
                );

        payment.setStatus(PaymentStatus.REFUND_PENDING);
        payment.setUpdatedAt(OffsetDateTime.now());

        PaymentTransaction transaction =
                PaymentTransaction.builder()
                        .paymentId(payment.getPaymentId())
                        .amount(payment.getAmount())
                        .status(
                                PaymentTransactionStatus.REFUND_PENDING
                        )
                        .createdAt(OffsetDateTime.now())
                        .build();

        transactionRepository.save(transaction);

        return mapToResponse(paymentRepository.save(payment));
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
}
