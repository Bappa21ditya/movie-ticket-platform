package com.movieTicket.InventoryService.kafka.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHeldEvent {

    private UUID eventId;

    private UUID sagaId;

    private UUID bookingId;

    private UUID userId;

    private Long showSeatId;

    private Long holdId;

    private OffsetDateTime expiresAt;

    private OffsetDateTime occurredAt;
}
