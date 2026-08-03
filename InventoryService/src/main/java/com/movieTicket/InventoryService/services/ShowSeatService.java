package com.movieTicket.InventoryService.services;

import com.movieTicket.InventoryService.dtos.CreateShowSeatRequest;
import com.movieTicket.InventoryService.dtos.ShowSeatResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShowSeatService {


    ShowSeatResponse createShowSeat(CreateShowSeatRequest request);

    ShowSeatResponse getShowSeat(Long showSeatId);

    List<ShowSeatResponse> getShowSeatsByShow(Long showId);

    List<ShowSeatResponse> getAvailableSeats(Long showId);
}
