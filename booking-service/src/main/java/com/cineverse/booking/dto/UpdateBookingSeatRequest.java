package com.cineverse.booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingSeatRequest {
    @NotBlank
    private String seatType;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal basePrice;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal finalPrice;
}
