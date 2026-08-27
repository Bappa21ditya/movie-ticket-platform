package com.cineverse.booking.dto.sagaClient;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatHoldResponse {

    private Long holdId;

    private Long showSeatId;

    private UUID bookingId;

    private UUID userId;

    private OffsetDateTime expiresAt;

    private HoldStatus status;

    private OffsetDateTime createdAt;
}
