package com.cineverse.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID showId;

    @NotEmpty
    @Valid
    private List<CreateBookingSeatRequest> seats;


    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal subtotal;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal discountAmount;

    @Size(max = 50)
    private String couponCode;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal couponDiscount;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal taxAmount;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal totalAmount;
}
