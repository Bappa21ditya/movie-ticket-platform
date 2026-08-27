package com.cineverse.booking.sagaServices;


import com.cineverse.booking.dto.sagaClient.CreateSeatHoldRequest;
import com.cineverse.booking.entity.Booking;
import com.cineverse.booking.entity.BookingSeat;
import com.cineverse.booking.exception.BookingNotFoundException;
import com.cineverse.booking.repository.BookingRepository;
import com.cineverse.booking.repository.BookingSeatRepository;
import com.cineverse.booking.restClient.InventoryClient;
import com.cineverse.booking.saga.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingSagaOrchestratorImpl implements BookingSagaOrchestrator{

    private final SagaInstanceRepository sagaInstanceRepository;
    private final BookingRepository bookingRepository;
    private  final BookingSeatRepository bookingSeatRepository;
    private final InventoryClient inventoryClient;

    @Override
    public void startSaga(UUID bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(bookingId)
                );

        SagaInstance saga = SagaInstance.builder()
                .bookingId(bookingId)
                .sagaType(SagaType.BOOKING_SAGA)
                .currentStep(SagaStep.STARTED)
                .status(SagaStatus.IN_PROGRESS)
                .retryCount(0)
                .startedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        SagaInstance savedSaga =sagaInstanceRepository.save(saga);

        // STARTED → HOLDING_SEAT

        savedSaga.setCurrentStep(SagaStep.HOLDING_SEAT);
        savedSaga.setUpdatedAt(Instant.now());

        sagaInstanceRepository.save(savedSaga);

        // Find seats belonging to this booking
        List<BookingSeat> bookingSeats =
                bookingSeatRepository
                        .findByBookingBookingId(bookingId);

        // Hold each seat in Inventory
        for (BookingSeat bookingSeat : bookingSeats) {

            CreateSeatHoldRequest request =
                    CreateSeatHoldRequest.builder()
                            .showSeatId(bookingSeat.getShowSeatId())
                            .bookingId(bookingId)
                            .userId(booking.getUserId())
                            .expiresAt(
                                    OffsetDateTime.now().plusMinutes(5)
                            )
                            .build();

            inventoryClient.createSeatHold(request);
        }

        // All seats successfully held
        savedSaga.setCurrentStep(SagaStep.SEAT_HELD);
        savedSaga.setUpdatedAt(Instant.now());

        sagaInstanceRepository.save(savedSaga);
    }
}
