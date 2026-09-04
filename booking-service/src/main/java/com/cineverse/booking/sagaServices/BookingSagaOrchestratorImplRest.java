package com.cineverse.booking.sagaServices;


import com.cineverse.booking.dto.sagaClient.CreateSeatHoldRequest;
import com.cineverse.booking.entity.Booking;
import com.cineverse.booking.entity.BookingSeat;
import com.cineverse.booking.enums.BookingStatus;
import com.cineverse.booking.exception.BookingNotFoundException;
import com.cineverse.booking.kafka.dtos.*;
import com.cineverse.booking.kafka.outbox.OutboxEvent;
import com.cineverse.booking.kafka.outbox.OutboxEventRepository;
import com.cineverse.booking.kafka.outbox.OutboxStatus;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingSagaOrchestratorImplRest implements BookingSagaOrchestrator {

    private final SagaInstanceRepository sagaInstanceRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final InventoryClient inventoryClient;
    private final PaymentService paymentService;
    private final SagaStateService sagaStateService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;


    // ============================================================
    // START SAGA
    // ============================================================

    @Override
    @Transactional
    public void startSaga(UUID bookingId) {

        System.out.println("===== START SAGA =====");
        System.out.println("Booking ID = " + bookingId);

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new BookingNotFoundException(bookingId)
                        );

        System.out.println("Booking found");

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


        System.out.println("Saga saved = " + savedSaga.getSagaId());


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


       //  Hold all seats

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




       //  STEP 2 - SEAT HELD

        savedSaga.setCurrentStep(
                SagaStep.SEAT_HELD
        );

        savedSaga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(savedSaga);


        // STEP 3 - PAYMENT IN PROGRESS


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


        // PAYMENT SUCCESS

        if (paymentResponse.getStatus()
                == PaymentStatus.SUCCESS) {

            handlePaymentSuccess(
                    savedSaga,
                    booking,
                    bookingSeats
            );

        }

        // PAYMENT FAILED

        else {

            handlePaymentFailure(
                    savedSaga,
                    booking,
                    bookingSeats
            );
        }
    }

    // PAYMENT SUCCESS FLOW

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

        // STEP 5 - CONFIRM SEATS

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


        // STEP 6 - CONFIRM BOOKING
        // for test make it false
        boolean simulateBookingFailure = false;
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

            return;
        }

// STEP 7 - SAGA COMPLETED

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


    // PAYMENT FAILURE FLOW

    private void handlePaymentFailure(
            SagaInstance saga,
            Booking booking,
            List<BookingSeat> bookingSeats) {

        System.out.println(
                "BEFORE PAYMENT_FAILED -> " +
                        saga.getCurrentStep()
        );



        // STEP - PAYMENT FAILED

        saga.setCurrentStep(
                SagaStep.PAYMENT_FAILED
        );

        saga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(saga);


        System.out.println(
                "AFTER PAYMENT_FAILED -> " +
                        saga.getCurrentStep()
        );
        // START COMPENSATION

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
        System.out.println(
                "AFTER COMPENSATING -> step=" +
                        saga.getCurrentStep() +
                        ", status=" +
                        saga.getStatus()
        );

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
            System.out.println("=================================");
            System.out.println("COMPENSATION CATCH REACHED");
            System.out.println("Saga ID = " + saga.getSagaId());
            System.out.println("Exception = " + ex.getMessage());
            System.out.println("=================================");


            sagaStateService.markCompensationFailed(
                    saga.getSagaId(),
                    ex
            );

            throw ex;
        }
    }

    // COMPENSATE / RECOVER SAGA
    @Override
    @Transactional
    public void retryCompensation(UUID sagaId) {

        // 1. FIND SAGA

        SagaInstance saga =
                sagaInstanceRepository.findById(sagaId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Saga not found: " + sagaId
                                )
                        );

        // 2. VALIDATE STATE

        if ((saga.getCurrentStep() != SagaStep.COMPENSATING
                && saga.getCurrentStep() != SagaStep.REFUND_PENDING)
                || saga.getStatus() != SagaStatus.IN_PROGRESS) {

            throw new IllegalStateException(
                    "Saga is not waiting for compensation retry"
            );
        }

        // 3. FIND BOOKING

        Booking booking =
                bookingRepository.findById(
                                saga.getBookingId()
                        )
                        .orElseThrow(() ->
                                new BookingNotFoundException(
                                        saga.getBookingId()
                                )
                        );

        try {

            // CASE 0: REFUND_PENDING RECOVERY

            if (saga.getCurrentStep()
                    == SagaStep.REFUND_PENDING) {

                System.out.println(
                        "===== REFUND PENDING RECOVERY ====="
                );

                System.out.println(
                        "Saga ID: " + saga.getSagaId()
                );

                System.out.println(
                        "Booking ID: " + saga.getBookingId()
                );

                // ONLY RETRY REFUND

                RefundResponse refundResponse =
                        paymentService.processPendingRefund(
                                saga.getBookingId()
                        );

                // REFUND STILL PENDING

                if (refundResponse.getStatus()
                        == RefundStatus.PENDING) {

                    saga.setRetryCount(
                            saga.getRetryCount() + 1
                    );

                    saga.setLastError(
                            "Refund still pending after recovery attempt."
                    );

                    saga.setCurrentStep(
                            SagaStep.REFUND_PENDING
                    );

                    saga.setStatus(
                            SagaStatus.IN_PROGRESS
                    );

                    saga.setUpdatedAt(
                            OffsetDateTime.now()
                    );

                    sagaInstanceRepository.save(saga);

                    return;
                }

                // REFUND FAILED

                if (refundResponse.getStatus()
                        != RefundStatus.SUCCESS) {

                    throw new IllegalStateException(
                            "Refund was not successful"
                    );
                }
                // CANCEL BOOKING

                booking.setStatus(
                        BookingStatus.CANCELLED
                );

                booking.setUpdatedAt(
                        OffsetDateTime.now()
                );

                bookingRepository.save(booking);

                // COMPENSATION COMPLETED

                markSagaCompensationCompleted(saga);

                System.out.println(
                        "===== REFUND RECOVERY SUCCESS ====="
                );

                return;
            }

            // FIND BOOKING SEATS


            List<BookingSeat> bookingSeats =
                    bookingSeatRepository
                            .findByBookingBookingId(
                                    saga.getBookingId()
                            );

            // CHECK COMPENSATION TYPE

            CompensationType compensationType =
                    saga.getCompensationType();

            // CASE 1: PAYMENT FAILED

            if (compensationType
                    == CompensationType.PAYMENT_FAILED) {

                /*
                 * Payment FAILED
                 *       ↓
                 * Seat was HELD
                 *       ↓
                 * Release held seats
                 *       ↓
                 * Cancel booking
                 *       ↓
                 * Saga FAILED
                 */
                releaseAllSeats(
                        bookingSeats,
                        saga.getBookingId()
                );

                booking.setStatus(
                        BookingStatus.CANCELLED
                );

                booking.setUpdatedAt(
                        OffsetDateTime.now()
                );

                bookingRepository.save(booking);

                markSagaCompensationCompleted(saga);

                return;
            }

            // CASE 2: BOOKING FAILED AFTER PAYMENT

            if (compensationType
                    == CompensationType.BOOKING_FAILED_AFTER_PAYMENT) {

                /*
                 * Payment SUCCESS
                 *       ↓
                 * Seats CONFIRMED
                 *       ↓
                 * Booking confirmation FAILED
                 *       ↓
                 * Release confirmed seats
                 *       ↓
                 * Refund
                 */

                // RELEASE CONFIRMED SEATS

                for (BookingSeat bookingSeat : bookingSeats) {

                    inventoryClient.releaseConfirmedSeat(
                            bookingSeat.getShowSeatId(),
                            saga.getBookingId()
                    );
                }

                // REFUND

                RefundResponse refundResponse =
                        paymentService.processPendingRefund(
                                saga.getBookingId()
                        );

                // REFUND PENDING

                if (refundResponse.getStatus()
                        == RefundStatus.PENDING) {

                    System.out.println(
                            "===== REFUND PENDING ====="
                    );

                    System.out.println(
                            "Saga ID: " + saga.getSagaId()
                    );

                    System.out.println(
                            "Booking ID: " + saga.getBookingId()
                    );

                    saga.setCurrentStep(
                            SagaStep.REFUND_PENDING
                    );

                    saga.setStatus(
                            SagaStatus.IN_PROGRESS
                    );

                    saga.setLastError(
                            "Refund pending. Waiting for refund recovery."
                    );

                    saga.setUpdatedAt(
                            OffsetDateTime.now()
                    );

                    sagaInstanceRepository.save(saga);

                    return;
                }

                // REFUND FAILED

                if (refundResponse.getStatus()
                        != RefundStatus.SUCCESS) {

                    throw new IllegalStateException(
                            "Refund was not successful"
                    );
                }

                // CANCEL BOOKING

                booking.setStatus(
                        BookingStatus.CANCELLED
                );

                booking.setUpdatedAt(
                        OffsetDateTime.now()
                );

                bookingRepository.save(booking);

                // COMPENSATION COMPLETED

                markSagaCompensationCompleted(saga);

                return;
            }

            // UNKNOWN COMPENSATION TYPE

            throw new IllegalStateException(
                    "Unknown compensation type: "
                            + compensationType
            );

        } catch (Exception ex) {

            // RECOVERY / COMPENSATION FAILED

            System.out.println(
                    "===== COMPENSATION RECOVERY FAILED ====="
            );

            System.out.println(
                    "Saga ID: " + saga.getSagaId()
            );

            System.out.println(
                    "Retry count: " + saga.getRetryCount()
            );

            System.out.println(
                    "Exception: " + ex.getClass().getName()
            );

            System.out.println(
                    "Message: " + ex.getMessage()
            );

            saga.setRetryCount(
                    saga.getRetryCount() + 1
            );

            saga.setLastError(
                    ex.getMessage()
            );

            /*
             * If refund recovery fails, keep it in
             * REFUND_PENDING so the scheduler can
             * retry it again.
             */
            if (saga.getCurrentStep()
                    != SagaStep.REFUND_PENDING) {

                saga.setCurrentStep(
                        SagaStep.COMPENSATING
                );
            }

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

    @Override
    public void handleSeatHeld(SeatHeldEvent event) {

    }

    @Override
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {

    }

    @Override
    public void handleSeatsConfirmed(
            SeatsConfirmedEvent event){}

    @Override
    public void handleSeatsReleased(SeatsReleasedEvent event) {

    }


    private void compensateAfterBookingFailure(
            SagaInstance saga,
            Booking booking,
            List<BookingSeat> bookingSeats) {

        try {
            // 1. RELEASE SEATS

            for (BookingSeat bookingSeat : bookingSeats) {

                inventoryClient.releaseConfirmedSeat(
                        bookingSeat.getShowSeatId(),
                        saga.getBookingId()
                );
            }

            // 2. REFUND PAYMENT

            RefundResponse refundResponse =
                    paymentService.refundPayment(
                            booking.getBookingId()
                    );


            if (refundResponse.getStatus() == RefundStatus.PENDING) {

                saga.setCurrentStep(
                        SagaStep.REFUND_PENDING
                );

                saga.setStatus(
                        SagaStatus.IN_PROGRESS
                );

                saga.setLastError(
                        "Refund pending. Waiting for retry."
                );

                saga.setUpdatedAt(
                        OffsetDateTime.now()
                );

                sagaInstanceRepository.save(saga);

                return;
            }

            if (refundResponse.getStatus() != RefundStatus.SUCCESS) {

                throw new IllegalStateException(
                        "Refund failed"
                );
            }

            // 3. CANCEL BOOKING

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

            // 4. COMPENSATION COMPLETED

            markSagaCompensationCompleted(saga);


        } catch (Exception ex) {

            sagaStateService.markCompensationFailed(
                    saga.getSagaId(),
                    ex
            );

            throw ex;
        }
    }


    // RELEASE ALL SEATS

    private void releaseAllSeats(
            List<BookingSeat> bookingSeats,
            UUID bookingId) {

        // only  for test make it true
        boolean simulateCompensationFailure = false;

        if (simulateCompensationFailure) {
            throw new RuntimeException(
                    "SIMULATED COMPENSATION FAILURE"
            );
        }

        for (BookingSeat bookingSeat : bookingSeats) {

            inventoryClient.releaseSeat(
                    bookingSeat.getShowSeatId(),
                    bookingId
            );

        }
    }

    // MARK BOOKING FAILED

    private void markBookingFailed(
            Booking booking) {

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

    // MARK SAGA COMPENSATION COMPLETED

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


