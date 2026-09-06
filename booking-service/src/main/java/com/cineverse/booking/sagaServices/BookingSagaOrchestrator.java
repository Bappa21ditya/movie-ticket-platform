package com.cineverse.booking.sagaServices;

import com.cineverse.booking.entity.Booking;
import com.cineverse.booking.entity.BookingSeat;
import com.cineverse.booking.kafka.dtos.*;
import com.cineverse.booking.saga.SagaInstance;

import java.util.List;
import java.util.UUID;

public interface BookingSagaOrchestrator {

    void startSaga(UUID bookingId);

    void retryCompensation(UUID sagaId);

    void handleSeatHeld(SeatHeldEvent event);

    public void handlePaymentSucceeded(PaymentSucceededEvent event);

    public void handleSeatsConfirmed(SeatsConfirmedEvent event);

    public void handleSeatsReleased(SeatsReleasedEvent event);

    public void processRefund(RefundRequestedEvent event);

     public void handlePaymentFailure(PaymentFailedEvent event);

    void handleRefundSucceeded(RefundSucceededEvent event);
}
