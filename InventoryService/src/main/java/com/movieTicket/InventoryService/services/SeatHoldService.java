package com.movieTicket.InventoryService.services;

import com.movieTicket.InventoryService.dtos.CreateSeatHoldRequest;
import com.movieTicket.InventoryService.dtos.SeatHoldResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface SeatHoldService {

    SeatHoldResponse createHold(CreateSeatHoldRequest request);

    SeatHoldResponse getHold(Long holdId);

    List<SeatHoldResponse> getHoldsByBooking(Long bookingId);

    List<SeatHoldResponse> getHoldsByUser(Long userId);

    public void confirmSeat(Long showSeatId, UUID bookingId);

    public boolean releaseSeat(Long showSeatId);

    public void releaseHold(Long showSeatId, UUID bookingId);

    public void compensateConfirmedSeat(Long showSeatId, UUID bookingId);
}
