package com.movieTicket.InventoryService.dtos;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import com.movieTicket.InventoryService.enums.SeatType;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private Long seatId;
    private Long screenId;
    private String rowNumber;
    private Integer seatNumber;
    private SeatType seatType;
}
