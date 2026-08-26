package com.cineverse.booking.controller;

import com.cineverse.booking.dto.CreateTicketRequest;
import com.cineverse.booking.dto.TicketResponse;
import com.cineverse.booking.dto.UpdateTicketRequest;
import com.cineverse.booking.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/bookings/{bookingId}/ticket")
    public ResponseEntity<TicketResponse> createTicket(
            @PathVariable UUID bookingId,
            @Valid @RequestBody CreateTicketRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ticketService.createTicket(
                                bookingId,
                                request
                        )
                );
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketResponse> getTicket(
            @PathVariable UUID ticketId) {

        return ResponseEntity.ok(
                ticketService.getTicket(ticketId)
        );
    }

    @GetMapping("/bookings/{bookingId}/ticket")
    public ResponseEntity<TicketResponse>
    getTicketByBooking(
            @PathVariable UUID bookingId) {

        return ResponseEntity.ok(
                ticketService.getTicketByBooking(
                        bookingId
                )
        );
    }

    @PutMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable UUID ticketId,
            @Valid @RequestBody UpdateTicketRequest request) {

        return ResponseEntity.ok(
                ticketService.updateTicket(
                        ticketId,
                        request
                )
        );
    }

    @DeleteMapping("/tickets/{ticketId}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable UUID ticketId) {

        ticketService.deleteTicket(ticketId);

        return ResponseEntity.noContent().build();
    }
}
