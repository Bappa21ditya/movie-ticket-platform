package com.cineverse.booking.exception;

import java.util.UUID;

public class BookingSeatNotFoundException  extends RuntimeException{

    public BookingSeatNotFoundException(UUID bookingSeatId) {
        super("Booking seat not found: " + bookingSeatId);
    }
}
