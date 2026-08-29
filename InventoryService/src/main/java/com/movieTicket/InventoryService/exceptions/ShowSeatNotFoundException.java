package com.movieTicket.InventoryService.exceptions;

public class ShowSeatNotFoundException extends RuntimeException{
    public ShowSeatNotFoundException(Long showSeatId) {
        super("Show seat not found with ID: " + showSeatId);
    }

    public ShowSeatNotFoundException(String message) {
        super(message);
    }
}
