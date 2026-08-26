package com.cineverse.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.cineverse.booking.enums.BookingStatus;

@Entity
@Table(
        name = "booking",
        indexes = {
                @Index(name = "idx_booking_user_id", columnList = "user_id"),
                @Index(name = "idx_booking_show_id", columnList = "show_id"),
                @Index(name = "idx_booking_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /*
     * Logical reference to Catalog Service.
     * No @ManyToOne because Catalog has a different database.
     */
    @Column(name = "show_id", nullable = false)
    private UUID showId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookingStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount",
            nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "coupon_discount",
            nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal couponDiscount;

    @Column(name = "tax_amount",
            nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount",
            nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Integer version;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();

        if (status == null) {
            status = BookingStatus.PENDING;
        }

        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }

        if (couponDiscount == null) {
            couponDiscount = BigDecimal.ZERO;
        }

        if (taxAmount == null) {
            taxAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
