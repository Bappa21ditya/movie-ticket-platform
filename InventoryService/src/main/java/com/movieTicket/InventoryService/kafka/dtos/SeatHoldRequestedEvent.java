package com.movieTicket.InventoryService.kafka.dtos;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHoldRequestedEvent {

    private UUID eventId;

    private UUID sagaId;

    private UUID bookingId;

    private UUID userId;

    private Long showSeatId;

    private OffsetDateTime expiresAt;

    private OffsetDateTime occurredAt;
}
