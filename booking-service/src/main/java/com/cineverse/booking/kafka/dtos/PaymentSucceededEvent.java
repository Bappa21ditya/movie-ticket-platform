package com.cineverse.booking.kafka.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSucceededEvent {

    private UUID eventId;

    private UUID sagaId;

    private UUID bookingId;

    private UUID userId;

    private UUID paymentId;

    private BigDecimal amount;

    private OffsetDateTime occurredAt;
}
