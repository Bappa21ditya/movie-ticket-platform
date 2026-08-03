package com.movieTicket.InventoryService.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSeatHoldRequest {
    @NotNull
    private Long showSeatId;

    @NotNull
    private Long bookingId;

    @NotNull
    private Long userId;

    @NotNull
    private LocalDateTime expiresAt;
}
