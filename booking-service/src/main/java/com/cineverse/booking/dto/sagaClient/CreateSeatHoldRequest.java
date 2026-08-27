package com.cineverse.booking.dto.sagaClient;

import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSeatHoldRequest {

    private Long showSeatId;

    private UUID bookingId;

    private UUID userId;

    private OffsetDateTime expiresAt;
}

