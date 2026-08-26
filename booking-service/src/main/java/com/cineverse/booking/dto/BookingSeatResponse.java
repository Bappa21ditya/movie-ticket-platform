package com.cineverse.booking.dto;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSeatResponse {

    private UUID bookingSeatId;

    private UUID bookingId;

    private UUID seatId;

    private String seatType;

    private BigDecimal basePrice;

    private BigDecimal finalPrice;
}
