package com.cineverse.booking.service;

import com.cineverse.booking.dto.CreateBookingSeatRequest;
import com.cineverse.booking.dto.UpdateBookingSeatRequest;
import com.cineverse.booking.dto.BookingSeatResponse;

import java.util.List;
import java.util.UUID;
public interface BookingSeatService {
    BookingSeatResponse addBookingSeat(
            UUID bookingId,
            CreateBookingSeatRequest request
    );

    BookingSeatResponse getBookingSeat(
            UUID bookingSeatId
    );

    List<BookingSeatResponse> getSeatsByBooking(
            UUID bookingId
    );

    BookingSeatResponse updateBookingSeat(
            UUID bookingSeatId,
            UpdateBookingSeatRequest request
    );

    void deleteBookingSeat(
            UUID bookingSeatId
    );
}
