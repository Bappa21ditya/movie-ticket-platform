package com.movieTicket.InventoryService.mapper;

import com.movieTicket.InventoryService.dtos.CreateSeatHoldRequest;
import com.movieTicket.InventoryService.dtos.SeatHoldResponse;
import com.movieTicket.InventoryService.entity.SeatHold;
import com.movieTicket.InventoryService.enums.HoldStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Component
public class SeatHoldMapper {
    public SeatHold toEntity(CreateSeatHoldRequest request) {

        return SeatHold.builder()
                .showSeatId(request.getShowSeatId())
                .bookingId(request.getBookingId())
                .userId(request.getUserId())
                .expiresAt(request.getExpiresAt())
                .status(HoldStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    public SeatHoldResponse toResponse(SeatHold hold) {

        return SeatHoldResponse.builder()
                .holdId(hold.getHoldId())
                .showSeatId(hold.getShowSeatId())
                .bookingId(hold.getBookingId())
                .userId(hold.getUserId())
                .expiresAt(hold.getExpiresAt())
                .status(hold.getStatus())
                .createdAt(hold.getCreatedAt())
                .build();
    }
}
