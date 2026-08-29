package com.cineverse.booking.dto.sagaClient;


import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseSeatRequest {
    private Long showSeatId;
    private UUID bookingId;
}
