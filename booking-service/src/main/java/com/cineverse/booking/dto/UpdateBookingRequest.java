package com.cineverse.booking.dto;

import com.cineverse.booking.enums.BookingStatus;
import lombok.*;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingRequest {

    @NotNull
    private BookingStatus status;

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
