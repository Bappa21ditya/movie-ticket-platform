package com.cineverse.booking.dto;

import com.cineverse.booking.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private UUID bookingId;

    private UUID userId;

    private UUID showId;

    private BookingStatus status;

    private BigDecimal subtotal;

    private BigDecimal discountAmount;

    private String couponCode;

    private BigDecimal couponDiscount;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private Integer version;
}
