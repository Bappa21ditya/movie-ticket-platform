package com.movieTicket.InventoryService.services;

import com.movieTicket.InventoryService.dtos.CreateSeatHoldRequest;
import com.movieTicket.InventoryService.dtos.SeatHoldResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SeatHoldService {

    SeatHoldResponse createHold(CreateSeatHoldRequest request);

    SeatHoldResponse getHold(Long holdId);

    List<SeatHoldResponse> getHoldsByBooking(Long bookingId);

    List<SeatHoldResponse> getHoldsByUser(Long userId);
}
