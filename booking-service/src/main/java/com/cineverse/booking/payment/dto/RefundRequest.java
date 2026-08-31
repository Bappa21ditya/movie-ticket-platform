package com.cineverse.booking.payment.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @NotNull
    private UUID bookingId;

    private String reason;
}
