package com.movieTicket.InventoryService.controller;

import com.movieTicket.InventoryService.dtos.ConfirmSeatRequest;
import com.movieTicket.InventoryService.dtos.CreateSeatHoldRequest;
import com.movieTicket.InventoryService.dtos.ReleaseSeatRequest;
import com.movieTicket.InventoryService.dtos.SeatHoldResponse;
import com.movieTicket.InventoryService.services.SeatHoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seat-holds")
@RequiredArgsConstructor
public class SeatHoldController {

    private final SeatHoldService seatHoldService;

    @PostMapping
    public ResponseEntity<SeatHoldResponse> createHold(
            @Valid @RequestBody CreateSeatHoldRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seatHoldService.createHold(request));
    }

    @GetMapping("/{holdId}")
    public ResponseEntity<SeatHoldResponse> getHold(
            @PathVariable Long holdId) {

        return ResponseEntity.ok(
                seatHoldService.getHold(holdId));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<SeatHoldResponse>> getHoldsByBooking(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                seatHoldService.getHoldsByBooking(bookingId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SeatHoldResponse>> getHoldsByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                seatHoldService.getHoldsByUser(userId));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmSeat(
            @RequestBody ConfirmSeatRequest request) {

        seatHoldService.confirmSeat(
                request.getShowSeatId(),
                request.getBookingId()
        );

        return ResponseEntity.ok().build();
    }
    @PostMapping("/release")
    public ResponseEntity<Void> releaseSeat(
            @RequestBody ReleaseSeatRequest request) {

        seatHoldService.releaseHold(
                request.getShowSeatId(),
                request.getBookingId()
        );

        return ResponseEntity.noContent().build();
    }
}
