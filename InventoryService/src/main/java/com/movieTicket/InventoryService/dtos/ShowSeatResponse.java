package com.movieTicket.InventoryService.dtos;
import com.movieTicket.InventoryService.enums.SeatStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatResponse {
    private Long showSeatId;
    private Long showId;
    private Long seatId;
    private SeatStatus status;
    private Long version;
}
