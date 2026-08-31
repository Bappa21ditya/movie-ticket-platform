package com.cineverse.booking.payment.service;

import com.cineverse.booking.payment.dto.CreatePaymentRequest;
import com.cineverse.booking.payment.dto.PaymentResponse;
import com.cineverse.booking.payment.dto.RefundResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    PaymentResponse getPayment(UUID paymentId);

    PaymentResponse getPaymentByBookingId(UUID bookingId);

    RefundResponse refundPayment(UUID paymentId);


}
