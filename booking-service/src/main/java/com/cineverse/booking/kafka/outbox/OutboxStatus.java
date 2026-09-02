package com.cineverse.booking.kafka.outbox;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
