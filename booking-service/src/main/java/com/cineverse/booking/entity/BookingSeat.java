package com.cineverse.booking.entity;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "booking_seat",
        indexes = {
                @Index(
                        name = "idx_booking_seat_booking_id",
                        columnList = "booking_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_seat_id")
    private UUID bookingSeatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "booking_id",
            nullable = false
    )
    private Booking booking;

    /*
     * Logical reference to Inventory Service.
     */
    @Column(name = "show_seat_id", nullable = false)
    private Long showSeatId;

    @Column(name = "seat_type", nullable = false, length = 30)
    private String seatType;

    @Column(
            name = "base_price",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal basePrice;

    @Column(
            name = "final_price",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal finalPrice;
}
