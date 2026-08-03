package com.movieTicket.InventoryService.dtos;
import com.movieTicket.InventoryService.enums.HoldStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatHoldResponse {
    private Long holdId;
    private Long showSeatId;
    private Long bookingId;
    private Long userId;
    private LocalDateTime expiresAt;
    private HoldStatus status;
    private LocalDateTime createdAt;
}
