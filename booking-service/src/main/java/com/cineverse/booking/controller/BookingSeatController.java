package com.cineverse.booking.controller;

import com.cineverse.booking.dto.CreateBookingSeatRequest;
import com.cineverse.booking.dto.UpdateBookingSeatRequest;
import com.cineverse.booking.dto.BookingSeatResponse;
import com.cineverse.booking.service.BookingSeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingSeatController {

    private final BookingSeatService bookingSeatService;

    @PostMapping("/{bookingId}/seats")
    public ResponseEntity<BookingSeatResponse> addBookingSeat(
            @PathVariable UUID bookingId,
            @Valid @RequestBody CreateBookingSeatRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        bookingSeatService.addBookingSeat(
                                bookingId,
                                request
                        )
                );
    }

    @GetMapping("/{bookingId}/seats")
    public ResponseEntity<List<BookingSeatResponse>>
    getBookingSeats(
            @PathVariable UUID bookingId) {

        return ResponseEntity.ok(
                bookingSeatService.getSeatsByBooking(
                        bookingId
                )
        );
    }

    @GetMapping("/seats/{bookingSeatId}")
    public ResponseEntity<BookingSeatResponse>
    getBookingSeat(
            @PathVariable UUID bookingSeatId) {

        return ResponseEntity.ok(
                bookingSeatService.getBookingSeat(
                        bookingSeatId
                )
        );
    }

    @PutMapping("/seats/{bookingSeatId}")
    public ResponseEntity<BookingSeatResponse>
    updateBookingSeat(
            @PathVariable UUID bookingSeatId,
            @Valid @RequestBody UpdateBookingSeatRequest request) {

        return ResponseEntity.ok(
                bookingSeatService.updateBookingSeat(
                        bookingSeatId,
                        request
                )
        );
    }

    @DeleteMapping("/seats/{bookingSeatId}")
    public ResponseEntity<Void> deleteBookingSeat(
            @PathVariable UUID bookingSeatId) {

        bookingSeatService.deleteBookingSeat(
                bookingSeatId
        );

        return ResponseEntity.noContent().build();
    }
}
