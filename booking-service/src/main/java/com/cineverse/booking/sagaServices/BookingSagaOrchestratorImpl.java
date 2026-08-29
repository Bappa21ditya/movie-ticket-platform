package com.cineverse.booking.sagaServices;


import com.cineverse.booking.dto.sagaClient.CreateSeatHoldRequest;
import com.cineverse.booking.dto.sagaClient.SeatHoldResponse;
import com.cineverse.booking.entity.Booking;
import com.cineverse.booking.entity.BookingSeat;
import com.cineverse.booking.enums.BookingStatus;
import com.cineverse.booking.exception.BookingNotFoundException;
import com.cineverse.booking.payment.dto.CreatePaymentRequest;
import com.cineverse.booking.payment.dto.PaymentResponse;
import com.cineverse.booking.payment.enums.PaymentMethod;
import com.cineverse.booking.payment.enums.PaymentStatus;
import com.cineverse.booking.payment.service.PaymentService;
import com.cineverse.booking.repository.BookingRepository;
import com.cineverse.booking.repository.BookingSeatRepository;
import com.cineverse.booking.restClient.InventoryClient;
import com.cineverse.booking.saga.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingSagaOrchestratorImpl implements BookingSagaOrchestrator {

    private final SagaInstanceRepository sagaInstanceRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final InventoryClient inventoryClient;
    private final PaymentService paymentService;

    @Override
    public void startSaga(UUID bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(bookingId)
                );

        SagaInstance savedSaga = SagaInstance.builder()
                .bookingId(bookingId)
                .sagaType(SagaType.BOOKING_SAGA)
                .currentStep(SagaStep.STARTED)
                .status(SagaStatus.IN_PROGRESS)
                .retryCount(0)
                .startedAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

         savedSaga = sagaInstanceRepository.save(savedSaga);

        // STEP 1: → HOLDING_SEAT

        savedSaga.setCurrentStep(SagaStep.HOLDING_SEAT);
        savedSaga.setUpdatedAt(OffsetDateTime.now());

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

            SeatHoldResponse holdResponse = inventoryClient.createSeatHold(request);
        }

        //  STEP 2 - SEAT HELD
        savedSaga.setCurrentStep(SagaStep.SEAT_HELD);
        savedSaga.setUpdatedAt(OffsetDateTime.now());

        sagaInstanceRepository.save(savedSaga);

        // STEP 3 - PAYMENT
        // =========================

        savedSaga.setCurrentStep(SagaStep.PAYMENT_IN_PROGRESS);
        savedSaga.setUpdatedAt(OffsetDateTime.now());

        sagaInstanceRepository.save(savedSaga);


        CreatePaymentRequest paymentRequest =
                CreatePaymentRequest.builder()
                        .bookingId(bookingId)
                        .amount(booking.getTotalAmount())
                        .paymentMethod(PaymentMethod.UPI)
                        .build();

        PaymentResponse paymentResponse =
                paymentService.createPayment(paymentRequest);


        // STEP 4 - PAYMENT SUCCESS

        if (paymentResponse.getStatus() == PaymentStatus.SUCCESS) {


            savedSaga.setCurrentStep(SagaStep.PAYMENT_SUCCESS);
            savedSaga.setUpdatedAt(OffsetDateTime.now());

            sagaInstanceRepository.save(savedSaga);


            // STEP 5 - CONFIRM SEATS
            savedSaga.setCurrentStep(SagaStep.CONFIRMING_BOOKING);
            savedSaga.setUpdatedAt(OffsetDateTime.now());

            sagaInstanceRepository.save(savedSaga);

            for (BookingSeat bookingSeat : bookingSeats) {

                inventoryClient.confirmSeat(
                        bookingSeat.getShowSeatId(),
                        bookingId
                );
            }


            // STEP 6 - CONFIRM BOOKING
            savedSaga.setCurrentStep(SagaStep.CONFIRMING_BOOKING);
            savedSaga.setUpdatedAt(OffsetDateTime.now());

            sagaInstanceRepository.save(savedSaga);


            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setUpdatedAt(OffsetDateTime.now());

            bookingRepository.save(booking);


            // STEP 7 - SAGA COMPLETED

            savedSaga.setCurrentStep(SagaStep.COMPLETED);
            savedSaga.setStatus(SagaStatus.COMPLETED);
            savedSaga.setCompletedAt(OffsetDateTime.now());
            savedSaga.setUpdatedAt(OffsetDateTime.now());

            sagaInstanceRepository.save(savedSaga);
        }
        else {

            // PAYMENT FAILED

            savedSaga.setCurrentStep(SagaStep.PAYMENT_FAILED);
            savedSaga.setUpdatedAt(OffsetDateTime.now());

            sagaInstanceRepository.save(savedSaga);


            // COMPENSATING

            savedSaga.setCurrentStep(SagaStep.COMPENSATING);
            savedSaga.setUpdatedAt(OffsetDateTime.now());

            sagaInstanceRepository.save(savedSaga);


            // RELEASE ALL HELD SEATS

            for (BookingSeat bookingSeat : bookingSeats) {

                inventoryClient.releaseSeat(
                        bookingSeat.getShowSeatId(),
                        bookingId
                );
            }
            // MARK BOOKING FAILED

            booking.setStatus(BookingStatus.CANCELLED);
            booking.setUpdatedAt(OffsetDateTime.now());

            bookingRepository.save(booking);

            // SAGA FAILED

            savedSaga.setCurrentStep(SagaStep.FAILED);
            savedSaga.setStatus(SagaStatus.FAILED);
            savedSaga.setUpdatedAt(OffsetDateTime.now());

            sagaInstanceRepository.save(savedSaga);
        }
    }

}
