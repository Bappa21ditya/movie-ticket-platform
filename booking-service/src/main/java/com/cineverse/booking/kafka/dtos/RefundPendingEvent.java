package com.cineverse.booking.kafka.dtos;


import java.time.OffsetDateTime;
import java.util.UUID;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundPendingEvent {
    private UUID eventId;

    private UUID sagaId;

    private UUID bookingId;

    private UUID refundId;

    private OffsetDateTime occurredAt;

    private String reason;
}
