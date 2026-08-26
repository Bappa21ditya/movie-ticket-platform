package com.cineverse.booking.entity;

import com.cineverse.booking.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "ticket",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ticket_number", columnNames = "ticket_number")
        },
        indexes = {
                @Index(name = "idx_ticket_booking_id", columnList = "booking_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ticket_id")
    private UUID ticketId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "booking_id",
            nullable = false,
            unique = true
    )
    private Booking booking;

    @Column(
            name = "ticket_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String ticketNumber;

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "qr_code", nullable = false, columnDefinition = "TEXT")
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status;

    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) {
            issuedAt = OffsetDateTime.now();
        }

        if (status == null) {
            status = TicketStatus.ISSUED;
        }
    }
}
