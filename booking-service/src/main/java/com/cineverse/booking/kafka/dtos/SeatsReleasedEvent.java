package com.cineverse.booking.kafka.dtos;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


import com.cineverse.booking.payment.enums.CompensationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatsReleasedEvent {
    private UUID eventId;
    private UUID sagaId;
    private UUID bookingId;
    private List<Long> showSeatIds;
    private CompensationType compensationType;
    private OffsetDateTime occurredAt;
}
