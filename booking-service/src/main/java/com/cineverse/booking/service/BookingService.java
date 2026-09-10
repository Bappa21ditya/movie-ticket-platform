package com.cineverse.booking.service;



import com.cineverse.booking.dto.BookingResponse;
import com.cineverse.booking.dto.CreateBookingRequest;
import com.cineverse.booking.dto.UpdateBookingRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Service
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
