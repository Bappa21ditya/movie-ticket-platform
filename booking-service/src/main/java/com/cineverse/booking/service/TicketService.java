package com.cineverse.booking.service;

import com.cineverse.booking.dto.CreateTicketRequest;
import com.cineverse.booking.dto.TicketResponse;
import com.cineverse.booking.dto.UpdateTicketRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface TicketService {

    TicketResponse createTicket(
            UUID bookingId,
            CreateTicketRequest request
    );

    TicketResponse getTicket(UUID ticketId);

    TicketResponse getTicketByBooking(UUID bookingId);

    TicketResponse updateTicket(
            UUID ticketId,
            UpdateTicketRequest request
    );

    void deleteTicket(UUID ticketId);
}
