package com.cineverse.booking.dto.sagaClient;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmSeatRequest {

    private Long showSeatId;
    private UUID bookingId;
}
