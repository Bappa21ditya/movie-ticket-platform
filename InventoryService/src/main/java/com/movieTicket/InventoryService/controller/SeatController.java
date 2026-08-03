package com.movieTicket.InventoryService.controller;

import com.movieTicket.InventoryService.dtos.CreateSeatRequest;
import com.movieTicket.InventoryService.dtos.SeatResponse;
import com.movieTicket.InventoryService.dtos.UpdateSeatRequest;
import com.movieTicket.InventoryService.services.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping
    public ResponseEntity<SeatResponse> createSeat(
            @Valid @RequestBody CreateSeatRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seatService.createSeat(request));
    }

    @GetMapping("/{seatId}")
    public ResponseEntity<SeatResponse> getSeat(
            @PathVariable Long seatId) {

        return ResponseEntity.ok(
                seatService.getSeat(seatId));
    }

    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<SeatResponse>> getSeatsByScreen(
            @PathVariable Long screenId) {

        return ResponseEntity.ok(
                seatService.getSeatsByScreen(screenId));
    }

    @PutMapping("/{seatId}")
    public ResponseEntity<SeatResponse> updateSeat(
            @PathVariable Long seatId,
            @Valid @RequestBody UpdateSeatRequest request) {

        return ResponseEntity.ok(
                seatService.updateSeat(seatId, request));
    }

    @DeleteMapping("/{seatId}")
    public ResponseEntity<Void> deleteSeat(
            @PathVariable Long seatId) {

        seatService.deleteSeat(seatId);

        return ResponseEntity.noContent().build();
    }
}
