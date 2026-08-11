package com.movieTicket.InventoryService.entity;

import com.movieTicket.InventoryService.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "show_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_show_seat",
                        columnNames = {"show_id", "seat_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long showSeatId;

    // ID of Show in Catalog Service
    @Column(nullable = false)
    private Long showId;

    // ID of Seat in this Inventory Service
    @Column(nullable = false)
    private Long seatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

  //  @Version
   // private Long version;
    // ============================================================ // OPTIMISTIC LOCKING // ============================================================ // // Hibernate automatically includes this version in UPDATEs. // // Example: // // User A reads: // version = 10 // // User B reads: // version = 10 // // User A updates: // version 10 → 11 SUCCESS // // User B tries: // UPDATE ... WHERE version = 10 // // But the database now contains version = 11. // // Therefore User B's UPDATE affects 0 rows, // and Hibernate throws an OptimisticLockException. // // ============================================================
}
