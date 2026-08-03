package com.movieTicket.InventoryService.dtos;
import com.movieTicket.InventoryService.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSeatRequest {
    @NotNull
    private Long screenId;

    @NotBlank
    private String rowNumber;

    @NotNull
    private Integer seatNumber;

    @NotNull
    private SeatType seatType;
}
