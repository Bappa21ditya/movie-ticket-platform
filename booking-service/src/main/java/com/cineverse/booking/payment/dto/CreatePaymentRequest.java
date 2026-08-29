package com.cineverse.booking.payment.dto;
import com.cineverse.booking.payment.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotNull
    private UUID bookingId;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal amount;

    @NotNull
    private PaymentMethod paymentMethod;
}
