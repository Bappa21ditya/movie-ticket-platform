package com.movieTicket.InventoryService.mapper;

import com.movieTicket.InventoryService.dtos.CreateSeatRequest;
import com.movieTicket.InventoryService.dtos.SeatResponse;
import com.movieTicket.InventoryService.dtos.UpdateSeatRequest;
import com.movieTicket.InventoryService.entity.Seat;
import org.springframework.stereotype.Component;

@Component
public class SeatMapper {
    public Seat toEntity(CreateSeatRequest request) {

        return Seat.builder()
                .screenId(request.getScreenId())
                .rowNumber(request.getRowNumber())
                .seatNumber(request.getSeatNumber())
                .seatType(request.getSeatType())
                .build();
    }

    public SeatResponse toResponse(Seat seat) {

        return SeatResponse.builder()
                .seatId(seat.getSeatId())
                .screenId(seat.getScreenId())
                .rowNumber(seat.getRowNumber())
                .seatNumber(seat.getSeatNumber())
                .seatType(seat.getSeatType())
                .build();
    }

    public Seat updateFromRequest(
            UpdateSeatRequest request,
            Seat seat) {

        seat.setRowNumber(request.getRowNumber());
        seat.setSeatNumber(request.getSeatNumber());
        seat.setSeatType(request.getSeatType());

        return seat;
    }
}
