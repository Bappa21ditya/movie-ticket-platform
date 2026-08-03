package com.movieTicket.InventoryService.services;

import com.movieTicket.InventoryService.dtos.CreateSeatRequest;
import com.movieTicket.InventoryService.dtos.SeatResponse;
import com.movieTicket.InventoryService.dtos.UpdateSeatRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SeatService {

    SeatResponse createSeat(CreateSeatRequest request);

    SeatResponse getSeat(Long seatId);

    List<SeatResponse> getSeatsByScreen(Long screenId);

    SeatResponse updateSeat(Long seatId, UpdateSeatRequest request);

    void deleteSeat(Long seatId);
}
