package com.cineverse.booking.sagaServices;

import java.util.UUID;

public interface BookingSagaOrchestrator {

    void startSaga(UUID bookingId);
}
