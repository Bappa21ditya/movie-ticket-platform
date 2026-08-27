package com.cineverse.booking.serviceImpl;

import com.cineverse.booking.dto.CreateBookingRequest;
import com.cineverse.booking.dto.UpdateBookingRequest;
import com.cineverse.booking.dto.BookingResponse;
import com.cineverse.booking.entity.Booking;
import com.cineverse.booking.entity.BookingSeat;
import com.cineverse.booking.enums.BookingStatus;
import com.cineverse.booking.exception.BookingNotFoundException;
import com.cineverse.booking.repository.BookingRepository;
import com.cineverse.booking.repository.BookingSeatRepository;
import com.cineverse.booking.sagaServices.BookingSagaOrchestrator;
import com.cineverse.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional

public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final BookingSagaOrchestrator sagaOrchestrator;

    private final BookingSeatRepository bookingSeatRepository;

    @Override
    public BookingResponse createBooking(
            CreateBookingRequest request) {

        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .showId(request.getShowId())
                .status(BookingStatus.PENDING)
                .subtotal(request.getSubtotal())
                .discountAmount(request.getDiscountAmount())
                .couponCode(request.getCouponCode())
                .couponDiscount(request.getCouponDiscount())
                .taxAmount(request.getTaxAmount())
                .totalAmount(request.getTotalAmount())
                .build();


        // 1. Save Booking first
        Booking saved = bookingRepository.save(booking);

        // 2. Create BookingSeat records
        List<BookingSeat> bookingSeats =
                request.getSeats()
                        .stream()
                        .map(seatRequest ->
                                BookingSeat.builder()
                                        .booking(saved)
                                        .showSeatId(seatRequest.getShowSeatId())
                                        .seatType(seatRequest.getSeatType())
                                        .basePrice(seatRequest.getBasePrice())
                                        .finalPrice(seatRequest.getFinalPrice())
                                        .build()
                        )
                        .toList();


        // 3. Save all seats
        bookingSeatRepository.saveAll(bookingSeats);


        sagaOrchestrator.startSaga(saved.getBookingId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID bookingId) {

        Booking booking = findBooking(bookingId);

        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {

        return bookingRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUser(
            UUID userId) {

        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByShow(
            UUID showId) {

        return bookingRepository.findByShowId(showId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BookingResponse updateBooking(
            UUID bookingId,
            UpdateBookingRequest request) {

        Booking booking = findBooking(bookingId);

        booking.setStatus(request.getStatus());
        booking.setSubtotal(request.getSubtotal());
        booking.setDiscountAmount(
                request.getDiscountAmount()
        );
        booking.setCouponCode(
                request.getCouponCode()
        );
        booking.setCouponDiscount(
                request.getCouponDiscount()
        );
        booking.setTaxAmount(
                request.getTaxAmount()
        );
        booking.setTotalAmount(
                request.getTotalAmount()
        );

        Booking updated = bookingRepository.save(booking);

        return mapToResponse(updated);
    }

    @Override
    public void deleteBooking(UUID bookingId) {

        Booking booking = findBooking(bookingId);

        bookingRepository.delete(booking);
    }

    private Booking findBooking(UUID bookingId) {

        return bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                bookingId
                        )
                );
    }

    private BookingResponse mapToResponse(
            Booking booking) {

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUserId())
                .showId(booking.getShowId())
                .status(booking.getStatus())
                .subtotal(booking.getSubtotal())
                .discountAmount(
                        booking.getDiscountAmount()
                )
                .couponCode(
                        booking.getCouponCode()
                )
                .couponDiscount(
                        booking.getCouponDiscount()
                )
                .taxAmount(booking.getTaxAmount())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .version(booking.getVersion())
                .build();
    }
}
