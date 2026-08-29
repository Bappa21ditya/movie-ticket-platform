package com.movieTicket.InventoryService.dtos;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseSeatRequest {
    private Long showSeatId;
    private UUID bookingId;
}
