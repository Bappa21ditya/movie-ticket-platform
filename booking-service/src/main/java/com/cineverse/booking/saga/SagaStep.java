package com.cineverse.booking.saga;

public enum SagaStep {

    STARTED,

    HOLDING_SEAT,
    SEAT_HELD,

    PAYMENT_IN_PROGRESS,
    PAYMENT_SUCCESS,

    CONFIRMING_BOOKING,

    COMPENSATING,

    COMPLETED,
    FAILED,

    REFUND_PENDING
}
