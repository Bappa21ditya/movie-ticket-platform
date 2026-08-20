package com.movieTicket.InventoryService.controller;

import com.movieTicket.InventoryService.dtos.CreateShowSeatRequest;
import com.movieTicket.InventoryService.dtos.ShowSeatResponse;
import com.movieTicket.InventoryService.services.ShowSeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/show-seats")
@RequiredArgsConstructor
public class ShowSeatController {

    private final ShowSeatService showSeatService;

    @PostMapping
    public ResponseEntity<ShowSeatResponse> createShowSeat(
            @Valid @RequestBody CreateShowSeatRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(showSeatService.createShowSeat(request));
    }

    @GetMapping("/{showSeatId}")
    public ResponseEntity<ShowSeatResponse> getShowSeat(
            @PathVariable Long showSeatId) {

        return ResponseEntity.ok(
                showSeatService.getShowSeat(showSeatId));
    }

    @GetMapping("/show/{showId}")
    public ResponseEntity<List<ShowSeatResponse>> getShowSeatsByShow(
            @PathVariable Long showId) {

        return ResponseEntity.ok(
                showSeatService.getShowSeatsByShow(showId));
    }

    @GetMapping("/show/{showId}/available")
    public ResponseEntity<List<ShowSeatResponse>> getAvailableSeats(
            @PathVariable Long showId) {

        return ResponseEntity.ok(
                showSeatService.getAvailableSeats(showId));
    }
    @PostMapping("/{showSeatId}/hold")
    public ResponseEntity<Boolean> holdSeat(@PathVariable Long showSeatId) {
        boolean success = showSeatService.holdSeat(showSeatId);
        if (success) {
            return ResponseEntity.ok(true);
        } else {
            // Return 409 Conflict if the seat was already held/booked by another request
            return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
        }
    }
}
