package com.cineverse.booking.service;


import com.cineverse.booking.dto.CreateBookingRequest;
import com.cineverse.booking.dto.UpdateBookingRequest;
import com.cineverse.booking.dto.BookingResponse;

import java.util.List;
import java.util.UUID;
public interface BookingService {

    BookingResponse createBooking(
            CreateBookingRequest request
    );

    BookingResponse getBooking(
            UUID bookingId
    );

    List<BookingResponse> getAllBookings();

    List<BookingResponse> getBookingsByUser(
            UUID userId
    );

    List<BookingResponse> getBookingsByShow(
            UUID showId
    );

    BookingResponse updateBooking(
            UUID bookingId,
            UpdateBookingRequest request
    );

    void deleteBooking(
            UUID bookingId
    );
}
