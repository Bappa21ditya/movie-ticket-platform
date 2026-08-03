package com.movieTicket.InventoryService.dtos;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShowSeatRequest {
    @NotNull
    private Long showId;

    @NotNull
    private Long seatId;
}
