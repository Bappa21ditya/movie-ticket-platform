package com.cineverse.booking.sagaServices;

import com.cineverse.booking.kafka.dtos.PaymentSucceededEvent;
import com.cineverse.booking.kafka.dtos.SeatHeldEvent;
import com.cineverse.booking.kafka.dtos.SeatsConfirmedEvent;
import com.cineverse.booking.kafka.dtos.SeatsReleasedEvent;

import java.util.UUID;

public interface BookingSagaOrchestrator {

    void startSaga(UUID bookingId);

    void retryCompensation(UUID sagaId);

    void handleSeatHeld(SeatHeldEvent event);

    public void handlePaymentSucceeded(PaymentSucceededEvent event);

    public void handleSeatsConfirmed(SeatsConfirmedEvent event);

    public void handleSeatsReleased(SeatsReleasedEvent event);
}
