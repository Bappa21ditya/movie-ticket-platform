package com.movieTicket.InventoryService.mapper;

import com.movieTicket.InventoryService.dtos.CreateShowSeatRequest;
import com.movieTicket.InventoryService.dtos.ShowSeatResponse;
import com.movieTicket.InventoryService.entity.ShowSeat;
import com.movieTicket.InventoryService.enums.SeatStatus;
import org.springframework.stereotype.Component;

@Component
public class ShowSeatMapper {
    public ShowSeat toEntity(CreateShowSeatRequest request) {

        return ShowSeat.builder()
                .showId(request.getShowId())
                .seatId(request.getSeatId())
                .status(SeatStatus.AVAILABLE)
           //     .version(0L)
                .build();
    }

    public ShowSeatResponse toResponse(ShowSeat showSeat) {

        return ShowSeatResponse.builder()
                .showSeatId(showSeat.getShowSeatId())
                .showId(showSeat.getShowId())
                .seatId(showSeat.getSeatId())
                .status(showSeat.getStatus())
           //     .version(showSeat.getVersion())
                .build();
    }
}
