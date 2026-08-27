package com.cineverse.booking.serviceImpl;



import com.cineverse.booking.dto.CreateBookingSeatRequest;
import com.cineverse.booking.dto.UpdateBookingSeatRequest;
import com.cineverse.booking.dto.BookingSeatResponse;
import com.cineverse.booking.entity.Booking;
import com.cineverse.booking.entity.BookingSeat;
import com.cineverse.booking.exception.BookingNotFoundException;
import com.cineverse.booking.exception.BookingSeatNotFoundException;
import com.cineverse.booking.repository.BookingRepository;
import com.cineverse.booking.repository.BookingSeatRepository;
import com.cineverse.booking.service.BookingSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingSeatServiceImpl implements BookingSeatService{
    private final BookingSeatRepository bookingSeatRepository;
    private final BookingRepository bookingRepository;

    @Override
    public BookingSeatResponse addBookingSeat(
            UUID bookingId,
            CreateBookingSeatRequest request) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(bookingId)
                );

        BookingSeat bookingSeat = BookingSeat.builder()
                .booking(booking)
                .showSeatId(request.getShowSeatId())
                .seatType(request.getSeatType())
                .basePrice(request.getBasePrice())
                .finalPrice(request.getFinalPrice())
                .build();

        return mapToResponse(
                bookingSeatRepository.save(bookingSeat)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BookingSeatResponse getBookingSeat(
            UUID bookingSeatId) {

        return mapToResponse(findBookingSeat(bookingSeatId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingSeatResponse> getSeatsByBooking(
            UUID bookingId) {

        return bookingSeatRepository
                .findByBookingBookingId(bookingId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BookingSeatResponse updateBookingSeat(
            UUID bookingSeatId,
            UpdateBookingSeatRequest request) {

        BookingSeat seat = findBookingSeat(bookingSeatId);

        seat.setSeatType(request.getSeatType());
        seat.setBasePrice(request.getBasePrice());
        seat.setFinalPrice(request.getFinalPrice());

        return mapToResponse(
                bookingSeatRepository.save(seat)
        );
    }

    @Override
    public void deleteBookingSeat(UUID bookingSeatId) {

        bookingSeatRepository.delete(
                findBookingSeat(bookingSeatId)
        );
    }

    private BookingSeat findBookingSeat(
            UUID bookingSeatId) {

        return bookingSeatRepository.findById(bookingSeatId)
                .orElseThrow(() ->
                        new BookingSeatNotFoundException(
                                bookingSeatId
                        )
                );
    }

    private BookingSeatResponse mapToResponse(
            BookingSeat seat) {

        return BookingSeatResponse.builder()
                .bookingSeatId(seat.getBookingSeatId())
                .bookingId(
                        seat.getBooking().getBookingId()
                )
                .showSeatId(seat.getShowSeatId())
                .seatType(seat.getSeatType())
                .basePrice(seat.getBasePrice())
                .finalPrice(seat.getFinalPrice())
                .build();
    }
}
