package com.cineverse.booking.sagaServices;

import com.cineverse.booking.dto.sagaClient.CreateSeatHoldRequest;
import com.cineverse.booking.entity.Booking;
import com.cineverse.booking.entity.BookingSeat;
import com.cineverse.booking.enums.BookingStatus;
import com.cineverse.booking.exception.BookingNotFoundException;
import com.cineverse.booking.payment.dto.CreatePaymentRequest;
import com.cineverse.booking.payment.dto.PaymentResponse;
import com.cineverse.booking.payment.dto.RefundResponse;
import com.cineverse.booking.payment.enums.CompensationType;
import com.cineverse.booking.payment.enums.PaymentMethod;
import com.cineverse.booking.payment.enums.PaymentStatus;
import com.cineverse.booking.payment.enums.RefundStatus;
import com.cineverse.booking.payment.service.PaymentService;
import com.cineverse.booking.repository.BookingRepository;
import com.cineverse.booking.repository.BookingSeatRepository;
import com.cineverse.booking.restClient.InventoryClient;
import com.cineverse.booking.saga.SagaInstance;
import com.cineverse.booking.saga.SagaInstanceRepository;
import com.cineverse.booking.saga.SagaStatus;
import com.cineverse.booking.saga.SagaStep;
import com.cineverse.booking.saga.SagaType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingSagaOrchestratorImpl
        implements BookingSagaOrchestrator {

    private final SagaInstanceRepository sagaInstanceRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final InventoryClient inventoryClient;
    private final PaymentService paymentService;
    private final SagaStateService sagaStateService;


    // ============================================================
    // START SAGA
    // ============================================================

    @Override
    public void startSaga(UUID bookingId) {

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new BookingNotFoundException(bookingId)
                        );

        // Create Saga
        SagaInstance savedSaga =
                SagaInstance.builder()
                        .bookingId(bookingId)
                        .sagaType(SagaType.BOOKING_SAGA)
                        .currentStep(SagaStep.STARTED)
                        .status(SagaStatus.IN_PROGRESS)
                        .retryCount(0)
                        .startedAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build();

        savedSaga =
                sagaInstanceRepository.save(savedSaga);


        // ========================================================
        // STEP 1 - HOLDING SEAT
        // ========================================================

        savedSaga.setCurrentStep(
                SagaStep.HOLDING_SEAT
        );

        savedSaga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(savedSaga);


        List<BookingSeat> bookingSeats =
                bookingSeatRepository
                        .findByBookingBookingId(bookingId);


        // Hold all seats
        for (BookingSeat bookingSeat : bookingSeats) {

            CreateSeatHoldRequest request =
                    CreateSeatHoldRequest.builder()
                            .showSeatId(
                                    bookingSeat.getShowSeatId()
                            )
                            .bookingId(bookingId)
                            .userId(booking.getUserId())
                            .expiresAt(
                                    OffsetDateTime.now()
                                            .plusMinutes(5)
                            )
                            .build();

            inventoryClient.createSeatHold(request);
        }


        // ========================================================
        // STEP 2 - SEAT HELD
        // ========================================================

        savedSaga.setCurrentStep(
                SagaStep.SEAT_HELD
        );

        savedSaga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(savedSaga);


        // ========================================================
        // STEP 3 - PAYMENT IN PROGRESS
        // ========================================================

        savedSaga.setCurrentStep(
                SagaStep.PAYMENT_IN_PROGRESS
        );

        savedSaga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(savedSaga);


        CreatePaymentRequest paymentRequest =
                CreatePaymentRequest.builder()
                        .bookingId(bookingId)
                        .amount(booking.getTotalAmount())
                        .paymentMethod(PaymentMethod.UPI)
                        .build();


        PaymentResponse paymentResponse =
                paymentService.createPayment(
                        paymentRequest
                );


        // ========================================================
        // PAYMENT SUCCESS
        // ========================================================

        if (paymentResponse.getStatus()
                == PaymentStatus.SUCCESS) {

            handlePaymentSuccess(
                    savedSaga,
                    booking,
                    bookingSeats
            );

        }

        // ========================================================
        // PAYMENT FAILED
        // ========================================================

        else {

            handlePaymentFailure(
                    savedSaga,
                    booking,
                    bookingSeats
            );
        }
    }


    // ============================================================
    // PAYMENT SUCCESS FLOW
    // ============================================================

    private void handlePaymentSuccess(
            SagaInstance saga,
            Booking booking,
            List<BookingSeat> bookingSeats) {


        // STEP 4 - PAYMENT SUCCESS

        saga.setCurrentStep(
                SagaStep.PAYMENT_SUCCESS
        );

        saga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(saga);


        // ========================================================
        // STEP 5 - CONFIRM SEATS
        // ========================================================

        saga.setCurrentStep(
                SagaStep.CONFIRMING_BOOKING
        );

        saga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(saga);


        for (BookingSeat bookingSeat : bookingSeats) {

            inventoryClient.confirmSeat(
                    bookingSeat.getShowSeatId(),
                    booking.getBookingId()
            );
        }


        // ========================================================
        // STEP 6 - CONFIRM BOOKING
        // ========================================================
        boolean simulateBookingFailure = true;
        try {

            /*
             * TEMPORARY FAILURE SIMULATION
             *
             * Use this while testing:
             *
             * Payment SUCCESS
             *       ↓
             * Seat CONFIRMED
             *       ↓
             * Booking update FAILS
             *       ↓
             * COMPENSATING
             *       ↓
             * Release seats
             *       ↓
             * Refund payment
             */


                booking.setStatus(
                        BookingStatus.CONFIRMED
                );

                booking.setUpdatedAt(
                        OffsetDateTime.now()
                );

                bookingRepository.save(booking);

                // TEMPORARY FAILURE SIMULATION
                if (simulateBookingFailure) {
                    throw new RuntimeException(
                            "Simulated booking update failure"
                    );
                }

            } catch (Exception ex) {

                // Booking confirmation failed
                markBookingFailed(booking);

                // Start compensation
                saga.setCurrentStep(
                        SagaStep.COMPENSATING
                );

                saga.setStatus(
                        SagaStatus.IN_PROGRESS
                );

                saga.setCompensationType(
                        CompensationType.BOOKING_FAILED_AFTER_PAYMENT
                );

                saga.setLastError(
                        ex.getMessage()
                );

                saga.setUpdatedAt(
                        OffsetDateTime.now()
                );

                sagaInstanceRepository.save(saga);

                // Try compensation immediately
                compensateAfterBookingFailure(
                        saga,
                        booking,
                        bookingSeats
                );

              //  throw ex;
            }


// ========================================================
// STEP 7 - SAGA COMPLETED
// ========================================================

            saga.setCurrentStep(
                    SagaStep.COMPLETED
            );

            saga.setStatus(
                    SagaStatus.COMPLETED
            );

            saga.setCompletedAt(
                    OffsetDateTime.now()
            );

            saga.setUpdatedAt(
                    OffsetDateTime.now()
            );

            sagaInstanceRepository.save(saga);
    }


    // ============================================================
    // PAYMENT FAILURE FLOW
    // ============================================================

    private void handlePaymentFailure(
            SagaInstance saga,
            Booking booking,
            List<BookingSeat> bookingSeats) {


        // STEP - PAYMENT FAILED

        saga.setCurrentStep(
                SagaStep.PAYMENT_FAILED
        );

        saga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(saga);


        // ========================================================
        // START COMPENSATION
        // ========================================================

        saga.setCurrentStep(
                SagaStep.COMPENSATING
        );

        saga.setStatus(
                SagaStatus.IN_PROGRESS
        );

        saga.setCompensationType(
                CompensationType.PAYMENT_FAILED
        );

        saga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(saga);


        try {

            // Release seats

            releaseAllSeats(
                    bookingSeats,
                    booking.getBookingId()
            );


            // Payment already failed.
            // Therefore NO REFUND is required.

            booking.setStatus(
                    BookingStatus.CANCELLED
            );

            booking.setUpdatedAt(
                    OffsetDateTime.now()
            );

            bookingRepository.save(booking);


            // Compensation completed

            markSagaCompensationCompleted(saga);


        } catch (Exception ex) {

            sagaStateService.markCompensationFailed(
                    saga.getSagaId(),
                    ex
            );

            throw ex;
        }
    }


    // ============================================================
    // RETRY COMPENSATION
    // ============================================================

    @Override
    public void retryCompensation(UUID sagaId) {

        // ========================================================
        // 1. FIND SAGA
        // ========================================================

        SagaInstance saga =
                sagaInstanceRepository.findById(sagaId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Saga not found: "
                                                + sagaId
                                )
                        );


        // ========================================================
        // 2. VALIDATE STATE
        // ========================================================

        if (saga.getCurrentStep()
                != SagaStep.COMPENSATING
                ||
                saga.getStatus()
                        != SagaStatus.IN_PROGRESS) {

            throw new IllegalStateException(
                    "Saga is not waiting for compensation retry"
            );
        }


        // ========================================================
        // 3. FIND BOOKING
        // ========================================================

        Booking booking =
                bookingRepository.findById(
                                saga.getBookingId()
                        )
                        .orElseThrow(() ->
                                new BookingNotFoundException(
                                        saga.getBookingId()
                                )
                        );


        // ========================================================
        // 4. FIND BOOKING SEATS
        // ========================================================

        List<BookingSeat> bookingSeats =
                bookingSeatRepository
                        .findByBookingBookingId(
                                saga.getBookingId()
                        );


        try {

            // ====================================================
            // 5. RELEASE SEATS
            // ====================================================

            releaseAllSeats(
                    bookingSeats,
                    saga.getBookingId()
            );


            // ====================================================
            // 6. CHECK COMPENSATION TYPE
            // ====================================================

            CompensationType compensationType =
                    saga.getCompensationType();


            // ====================================================
            // PAYMENT_FAILED
            // ====================================================

            if (compensationType
                    == CompensationType.PAYMENT_FAILED) {

                /*
                 * Payment itself failed.
                 *
                 * Therefore:
                 *
                 * Release Seat
                 *       ↓
                 * Cancel Booking
                 *
                 * NO REFUND
                 */
            }


            // ====================================================
            // BOOKING_FAILED_AFTER_PAYMENT
            // ====================================================

            else if (compensationType
                    == CompensationType.BOOKING_FAILED_AFTER_PAYMENT) {

                /*
                 * Payment succeeded.
                 *
                 * Booking update failed.
                 *
                 * Therefore:
                 *
                 * Release Seat
                 *       ↓
                 * Refund Payment
                 *       ↓
                 * Cancel Booking
                 */

                RefundResponse refundResponse =
                        paymentService.refundPayment(
                                saga.getBookingId()
                        );


                if (refundResponse.getStatus()
                        != RefundStatus.SUCCESS) {

                    throw new IllegalStateException(
                            "Refund was not successful"
                    );
                }
            }


            // ====================================================
            // 7. CANCEL BOOKING
            // ====================================================

            booking.setStatus(
                    BookingStatus.CANCELLED
            );

            booking.setUpdatedAt(
                    OffsetDateTime.now()
            );

            bookingRepository.save(booking);


            // ====================================================
            // 8. COMPENSATION COMPLETED
            // ====================================================

            markSagaCompensationCompleted(saga);


        } catch (Exception ex) {

            // ====================================================
            // RETRY FAILED
            // ====================================================

            saga.setRetryCount(
                    saga.getRetryCount() + 1
            );

            saga.setLastError(
                    ex.getMessage()
            );

            saga.setCurrentStep(
                    SagaStep.COMPENSATING
            );

            saga.setStatus(
                    SagaStatus.IN_PROGRESS
            );

            saga.setUpdatedAt(
                    OffsetDateTime.now()
            );

            sagaInstanceRepository.save(saga);

            throw ex;
        }
    }


    // ============================================================
    // COMPENSATE AFTER BOOKING FAILURE
    // ============================================================

    private void compensateAfterBookingFailure(
            SagaInstance saga,
            Booking booking,
            List<BookingSeat> bookingSeats) {

        try {

            // ====================================================
            // 1. RELEASE SEATS
            // ====================================================

//            releaseAllSeats(
//                    bookingSeats,
//                    booking.getBookingId()
//            );
            for (BookingSeat bookingSeat : bookingSeats) {

                inventoryClient.releaseConfirmedSeat(
                        bookingSeat.getShowSeatId(),
                        saga.getBookingId()
                );
            }


            // ====================================================
            // 2. REFUND PAYMENT
            // ====================================================

            RefundResponse refundResponse =
                    paymentService.refundPayment(
                            booking.getBookingId()
                    );


            if (refundResponse.getStatus()
                    != RefundStatus.SUCCESS) {

                throw new IllegalStateException(
                        "Refund was not successful"
                );
            }


            // ====================================================
            // 3. CANCEL BOOKING
            // ====================================================

            Booking freshBooking =
                    bookingRepository.findById(
                            booking.getBookingId()
                    ).orElseThrow(() ->
                            new BookingNotFoundException(
                                    booking.getBookingId()
                            )
                    );

            freshBooking.setStatus(
                    BookingStatus.CANCELLED
            );

            freshBooking.setUpdatedAt(
                    OffsetDateTime.now()
            );

            bookingRepository.save(freshBooking);


            // ====================================================
            // 4. COMPENSATION COMPLETED
            // ====================================================

            markSagaCompensationCompleted(saga);


        } catch (Exception ex) {

            sagaStateService.markCompensationFailed(
                    saga.getSagaId(),
                    ex
            );

            throw ex;
        }
    }


    // ============================================================
    // RELEASE ALL SEATS
    // ============================================================

    private void releaseAllSeats(
            List<BookingSeat> bookingSeats,
            UUID bookingId) {

        for (BookingSeat bookingSeat : bookingSeats) {

            inventoryClient.releaseSeat(
                    bookingSeat.getShowSeatId(),
                    bookingId
            );
        }
    }


    // ============================================================
    // MARK BOOKING FAILED
    // ============================================================

    private void markBookingFailed(
            Booking bookings) {

        Booking booking =
                bookingRepository.findById(bookings.getBookingId())
                        .orElseThrow(() ->
                                new BookingNotFoundException(bookings.getBookingId())
                        );
        Booking freshBooking =
                bookingRepository.findById(
                                booking.getBookingId()
                        )
                        .orElseThrow(() ->
                                new BookingNotFoundException(
                                        booking.getBookingId()
                                )
                        );


        freshBooking.setStatus(
                BookingStatus.FAILED
        );

        freshBooking.setUpdatedAt(
                OffsetDateTime.now()
        );

        bookingRepository.save(freshBooking);
    }


    // ============================================================
    // MARK SAGA COMPENSATION COMPLETED
    // ============================================================

    private void markSagaCompensationCompleted(
            SagaInstance saga) {

        /*
         * Business operation failed,
         * but compensation succeeded.
         *
         * Therefore Saga is finished.
         */

        saga.setCurrentStep(
                SagaStep.FAILED
        );

        saga.setStatus(
                SagaStatus.FAILED
        );

        saga.setLastError(null);

        saga.setCompletedAt(
                OffsetDateTime.now()
        );

        saga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(saga);
    }
}

