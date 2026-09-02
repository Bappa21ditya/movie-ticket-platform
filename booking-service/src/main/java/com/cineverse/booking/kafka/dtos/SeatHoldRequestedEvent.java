package com.cineverse.booking.kafka.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatHoldRequestedEvent {
    UUID eventId;
    UUID sagaId;
    UUID bookingId;
    UUID userId;
    Long showSeatId;
    OffsetDateTime expiresAt;
    OffsetDateTime occurredAt;
}
