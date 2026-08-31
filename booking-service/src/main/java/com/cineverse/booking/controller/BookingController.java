package com.cineverse.booking.controller;


import com.cineverse.booking.dto.CreateBookingRequest;
import com.cineverse.booking.dto.UpdateBookingRequest;
import com.cineverse.booking.dto.BookingResponse;
import com.cineverse.booking.sagaServices.BookingSagaOrchestrator;
import com.cineverse.booking.service.BookingService;
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
public class BookingController {

    private final BookingService bookingService;

    private final BookingSagaOrchestrator bookingSagaOrchestrator;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        bookingService.createBooking(request)
                );
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable UUID bookingId) {

        return ResponseEntity.ok(
                bookingService.getBooking(bookingId)
        );
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {

        return ResponseEntity.ok(
                bookingService.getAllBookings()
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                bookingService.getBookingsByUser(userId)
        );
    }

    @GetMapping("/show/{showId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByShow(
            @PathVariable UUID showId) {

        return ResponseEntity.ok(
                bookingService.getBookingsByShow(showId)
        );
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable UUID bookingId,
            @Valid @RequestBody UpdateBookingRequest request) {

        return ResponseEntity.ok(
                bookingService.updateBooking(
                        bookingId,
                        request
                )
        );
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable UUID bookingId) {

        bookingService.deleteBooking(bookingId);

        return ResponseEntity.noContent().build();
    }
    @PostMapping("/saga/{sagaId}/retry-compensation")
    public ResponseEntity<String> retryCompensation(
            @PathVariable UUID sagaId) {

        bookingSagaOrchestrator.retryCompensation(sagaId);

        return ResponseEntity.ok(
                "Compensation retry completed"
        );
    }
}
