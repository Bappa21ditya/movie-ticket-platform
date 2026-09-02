package com.cineverse.booking.sagaServices;

import com.cineverse.booking.kafka.dtos.SeatHeldEvent;

import java.util.UUID;

public interface BookingSagaOrchestrator {

    void startSaga(UUID bookingId);

    void retryCompensation(UUID sagaId);

    void handleSeatHeld(SeatHeldEvent event);
}
