package com.cineverse.booking.payment.entity;

import com.cineverse.booking.payment.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "refund",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_refund_payment",
                        columnNames = "payment_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    @Id
    @GeneratedValue
    private UUID refundId;

    @Column(nullable = false)
    private UUID paymentId;

    @Column(nullable = false)
    private UUID bookingId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    private String reason;

    private Integer retryCount;

    private String lastError;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
