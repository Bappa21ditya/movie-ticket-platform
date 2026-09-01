package com.cineverse.booking.payment.controller;

import com.cineverse.booking.payment.dto.CreatePaymentRequest;
import com.cineverse.booking.payment.dto.PaymentResponse;
import com.cineverse.booking.payment.dto.RefundResponse;
import com.cineverse.booking.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.createPayment(request));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable UUID paymentId) {

        return ResponseEntity.ok(
                paymentService.getPayment(paymentId)
        );
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getByBookingId(
            @PathVariable UUID bookingId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByBookingId(bookingId)
        );
    }

    @PostMapping("/{bookingId}/refund")
    public ResponseEntity<RefundResponse> refund(
            @PathVariable UUID bookingId) {

        return ResponseEntity.ok(
                paymentService.refundPayment(bookingId)
        );
    }

}
