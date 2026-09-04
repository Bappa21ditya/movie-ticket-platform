package com.cineverse.booking.kafka.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatConfirmRequestedEvent {
    private UUID eventId;

    private UUID sagaId;

    private UUID bookingId;

    private List<Long> showSeatIds;

    private OffsetDateTime occurredAt;
}
