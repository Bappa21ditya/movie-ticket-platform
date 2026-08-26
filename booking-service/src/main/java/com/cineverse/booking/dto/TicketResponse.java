package com.cineverse.booking.dto;

import com.cineverse.booking.enums.TicketStatus;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private UUID ticketId;

    private UUID bookingId;

    private String ticketNumber;

    private OffsetDateTime issuedAt;

    private String qrCode;

    private TicketStatus status;
}
