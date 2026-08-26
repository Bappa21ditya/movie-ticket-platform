package com.cineverse.booking.exception;

import java.util.UUID;

public class TicketNotFoundException extends RuntimeException{
    public TicketNotFoundException(UUID ticketId) {
        super("Ticket not found: " + ticketId);
    }

    public TicketNotFoundException(
            String message) {
        super(message);
    }
}
