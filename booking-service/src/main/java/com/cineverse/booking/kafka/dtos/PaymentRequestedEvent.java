package com.cineverse.booking.kafka.dtos;

import com.cineverse.booking.payment.enums.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestedEvent {

    private UUID eventId;

    private UUID sagaId;

    private UUID bookingId;

    private UUID userId;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    private OffsetDateTime occurredAt;
}
