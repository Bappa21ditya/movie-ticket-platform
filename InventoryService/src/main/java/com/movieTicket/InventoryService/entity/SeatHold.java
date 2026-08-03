package com.movieTicket.InventoryService.entity;

import com.movieTicket.InventoryService.enums.HoldStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_holds")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatHold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holdId;

    // ID of ShowSeat in Inventory Service
    @Column(nullable = false)
    private Long showSeatId;

    // ID of Booking in Booking Service
    @Column(nullable = false)
    private Long bookingId;

    // ID of User from authentication system
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HoldStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
