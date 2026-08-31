package com.cineverse.booking.payment.dto;


import com.cineverse.booking.payment.enums.RefundStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {

    private UUID refundId;
    private UUID bookingId;
    private BigDecimal amount;
    private RefundStatus status;
}
