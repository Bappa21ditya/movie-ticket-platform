package com.cineverse.booking.sagaServices;

import com.cineverse.booking.entity.Booking;
import com.cineverse.booking.entity.BookingSeat;
import com.cineverse.booking.enums.BookingStatus;
import com.cineverse.booking.exception.BookingNotFoundException;
import com.cineverse.booking.kafka.dtos.*;
import com.cineverse.booking.kafka.outbox.OutboxEvent;
import com.cineverse.booking.kafka.outbox.OutboxEventRepository;
import com.cineverse.booking.kafka.outbox.OutboxStatus;
import com.cineverse.booking.payment.dto.RefundResponse;
import com.cineverse.booking.payment.entity.Refund;
import com.cineverse.booking.payment.enums.CompensationType;
import com.cineverse.booking.payment.enums.PaymentMethod;
import com.cineverse.booking.payment.enums.RefundStatus;
import com.cineverse.booking.payment.repos.RefundRepository;
import com.cineverse.booking.payment.service.PaymentService;
import com.cineverse.booking.repository.BookingRepository;
import com.cineverse.booking.repository.BookingSeatRepository;
import com.cineverse.booking.saga.SagaInstance;
import com.cineverse.booking.saga.SagaInstanceRepository;
import com.cineverse.booking.saga.SagaStatus;
import com.cineverse.booking.saga.SagaStep;
import com.cineverse.booking.saga.SagaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Primary
public class BookingSagaOrchestratorImpl
        implements BookingSagaOrchestrator {

    private final SagaInstanceRepository sagaInstanceRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentService paymentService;
    private final SagaStateService sagaStateService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final RefundRepository refundRepository;


    // START SAGA

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


        // STEP 1 - HOLDING SEAT

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


        // CREATE OUTBOX EVENTS
        for (BookingSeat bookingSeat : bookingSeats) {

            OffsetDateTime expiresAt =
                    OffsetDateTime.now()
                            .plusMinutes(5);

            // 1. Create domain event

            SeatHoldRequestedEvent event =
                    SeatHoldRequestedEvent.builder()
                            .eventId(UUID.randomUUID())
                            .sagaId(savedSaga.getSagaId())
                            .bookingId(bookingId)
                            .userId(booking.getUserId())
                            .showSeatId(
                                    bookingSeat.getShowSeatId()
                            )
                            .expiresAt(expiresAt)
                            .occurredAt(
                                    OffsetDateTime.now()
                            )
                            .build();

            // 2. Serialize event to JSON

            JsonNode payload;

            try {

                payload = objectMapper.valueToTree(event);
                // objectMapper.writeValueAsString(event);

            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(
                        "Failed to serialize SeatHoldRequestedEvent",
                        ex
                );
            }

            System.out.println("Payload = " + payload);

            // 3. Save event in Outbox

            OutboxEvent outboxEvent =
                    OutboxEvent.builder()
                            .eventId(event.getEventId())
                            .aggregateId(bookingId)
                            .aggregateType("BOOKING")
                            .eventType("SEAT_HOLD_REQUESTED")
                            .payload(payload)
                            .status(OutboxStatus.PENDING)
                            .retryCount(0)
                            .createdAt(OffsetDateTime.now())
                            .build();


            //  outboxEventRepository.save(outboxEvent);
            outboxEventRepository.saveAndFlush(outboxEvent);

            System.out.println(
                    "OUTBOX SAVED = " + outboxEvent.getEventId()
            );
        }
        System.out.println("===== END SAGA =====");
    }

    // PAYMENT SUCCESS FLOW

    @Override
    @Transactional
    public void handleSeatHeld(SeatHeldEvent event) {

        System.out.println("===== HANDLE SEAT HELD =====");

        System.out.println(
                "Saga ID = " + event.getSagaId()
        );

        System.out.println(
                "Booking ID = " + event.getBookingId()
        );

        System.out.println(
                "ShowSeat ID = " + event.getShowSeatId()
        );

        System.out.println(
                "Hold ID = " + event.getHoldId()
        );

        SagaInstance saga =
                sagaInstanceRepository
                        .findById(event.getSagaId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Saga not found: "
                                                + event.getSagaId()
                                )
                        );

        Booking booking =
                bookingRepository
                        .findById(event.getBookingId())
                        .orElseThrow(() ->
                                new BookingNotFoundException(
                                        event.getBookingId()
                                )
                        );

        // VALIDATE SAGA STATE

        if (saga.getStatus() != SagaStatus.IN_PROGRESS) {

            System.out.println(
                    "Ignoring SEAT_HELD because Saga is not IN_PROGRESS"
            );

            return;
        }

        if (saga.getCurrentStep() != SagaStep.HOLDING_SEAT) {

            System.out.println(
                    "Ignoring SEAT_HELD because Saga is currently at "
                            + saga.getCurrentStep()
            );

            return;
        }

        // SEAT HELD
        System.out.println("1. BEFORE Seat held ");
        saga.setCurrentStep(
                SagaStep.SEAT_HELD
        );
        System.out.println("2. BEFORE date update");
        saga.setUpdatedAt(
                OffsetDateTime.now()
        );
        System.out.println("3. BEFORE SAGA SAVE");
        SagaInstance saved = sagaInstanceRepository.save(saga);

        System.out.println(
                "4. AFTER SAGA SAVE, STEP = "
                        + saved.getCurrentStep()
        );

        System.out.println(
                "Saga moved to SEAT_HELD"
        );

        // NEXT STEP - PAYMENT

        saga.setCurrentStep(
                SagaStep.PAYMENT_IN_PROGRESS
        );

        saga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(saga);

        System.out.println(
                "Saga moved to PAYMENT_IN_PROGRESS"
        );

        // CREATE PAYMENT

        PaymentRequestedEvent paymentEvent =
                PaymentRequestedEvent.builder()
                        .eventId(UUID.randomUUID())
                        .sagaId(saga.getSagaId())
                        .bookingId(event.getBookingId())
                        .userId(event.getUserId())
                        .amount(booking.getTotalAmount())
                        .paymentMethod(PaymentMethod.UPI)
                        .occurredAt(OffsetDateTime.now())
                        .build();

        JsonNode payload =
                objectMapper.valueToTree(paymentEvent);


        OutboxEvent outboxEvent =
                OutboxEvent.builder()
                        .eventId(paymentEvent.getEventId())
                        .aggregateId(event.getBookingId())
                        .aggregateType("BOOKING")
                        .eventType("PAYMENT_REQUESTED")
                        .payload(payload)
                        .status(OutboxStatus.PENDING)
                        .retryCount(0)
                        .createdAt(OffsetDateTime.now())
                        .build();

        outboxEventRepository.saveAndFlush(
                outboxEvent
        );

        System.out.println(
                "PAYMENT_REQUESTED OUTBOX SAVED = "
                        + paymentEvent.getEventId()
        );

    }

    @Override
    @Transactional
    public void handlePaymentSucceeded(
            PaymentSucceededEvent event) {

        System.out.println(
                "===== HANDLE PAYMENT SUCCEEDED ====="
        );

        System.out.println(
                "Saga ID = " + event.getSagaId()
        );

        System.out.println(
                "Booking ID = " + event.getBookingId()
        );

        // 1. FIND SAGA
        SagaInstance saga =
                sagaInstanceRepository
                        .findById(event.getSagaId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Saga not found: "
                                                + event.getSagaId()
                                )
                        );

        // 2. FIND BOOKING
        Booking booking =
                bookingRepository
                        .findById(event.getBookingId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Booking not found: "
                                                + event.getBookingId()
                                )
                        );

        // 3. VALIDATE SAGA STATE
        if (saga.getCurrentStep()
                != SagaStep.PAYMENT_IN_PROGRESS) {

            System.out.println(
                    "Ignoring PAYMENT_SUCCEEDED. "
                            + "Current saga step = "
                            + saga.getCurrentStep()
            );

            return;
        }

        // 4. PAYMENT SUCCESS
        saga.setCurrentStep(
                SagaStep.PAYMENT_SUCCESS
        );

        saga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(saga);

        System.out.println(
                "Saga transitioned to PAYMENT_SUCCEEDED"
        );


        // STEP 5 - GET BOOKING SEATS

        List<BookingSeat> bookingSeats =
                bookingSeatRepository
                        .findByBookingBookingId(
                                booking.getBookingId()
                        );


        if (bookingSeats.isEmpty()) {

            throw new IllegalStateException(
                    "No booking seats found for booking: "
                            + booking.getBookingId()
            );
        }

        System.out.println(
                "Booking seats = "
                        + bookingSeats.size()
        );

        List<Long> showSeatIds =
                bookingSeats.stream()
                        .map(BookingSeat::getShowSeatId)
                        .toList();

        System.out.println(
                "Seats to confirm = "
                        + showSeatIds
        );

        // STEP  6-TRANSITION SAGA -> CONFIRMING_BOOKING


        saga.setCurrentStep(
                SagaStep.CONFIRMING_BOOKING
        );

        saga.setUpdatedAt(
                OffsetDateTime.now()
        );

        sagaInstanceRepository.save(saga);

        ///  CREATE SEAT CONFIRM REQUEST EVENT


        SeatConfirmRequestedEvent confirmEvent =
                SeatConfirmRequestedEvent.builder()

                        .eventId(
                                UUID.randomUUID()
                        )

                        .sagaId(
                                saga.getSagaId()
                        )

                        .bookingId(
                                booking.getBookingId()
                        )

                        .showSeatIds(
                                showSeatIds
                        )

                        .occurredAt(
                                OffsetDateTime.now()
                        )

                        .build();

        // 8. SAVE TO OUTBOX

        JsonNode payload =
                objectMapper.valueToTree(confirmEvent);
        OutboxEvent outboxEvent =
                OutboxEvent.builder()

                        .eventId(
                                confirmEvent.getEventId()
                        )

                        .aggregateId(
                                booking.getBookingId()
                        )

                        .aggregateType(
                                "BOOKING"
                        )

                        .eventType(
                                "SEAT_CONFIRM_REQUESTED"
                        )

                        .payload(
                                payload)

                        .status(
                                OutboxStatus.PENDING
                        )

                        .retryCount(
                                0
                        )

                        .createdAt(
                                OffsetDateTime.now()
                        )

                        .build();


        outboxEventRepository.save(outboxEvent);

        System.out.println(
                "SEAT_CONFIRM_REQUESTED OUTBOX SAVED = "
                        + confirmEvent.getEventId()
        );
        System.out.println(
                "===== END HANDLE PAYMENT SUCCEEDED ====="
        );
    }

    // PAYMENT FAILURE FLOW

    public void handlePaymentFailure(PaymentFailedEvent event) {

        {

            System.out.println(
                    "===== HANDLE PAYMENT FAILURE ====="
            );

            UUID sagaId = event.getSagaId();
            UUID bookingId = event.getBookingId();

            System.out.println("Saga ID = " + sagaId);
            System.out.println("Booking ID = " + bookingId);

            SagaInstance saga =
                    sagaInstanceRepository
                            .findById(sagaId)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Saga not found: " + sagaId
                                    )
                            );

            Booking booking =
                    bookingRepository
                            .findById(bookingId)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Booking not found: " + bookingId
                                    )
                            );

            /*
             * Idempotency
             */
            if (saga.getStatus() == SagaStatus.COMPLETED
                    || saga.getStatus() == SagaStatus.FAILED) {

                System.out.println(
                        "Saga already completed/failed. Ignoring duplicate event."
                );

                return;
            }

            /*
             * Validate expected state
             */
            if (saga.getCurrentStep() != SagaStep.PAYMENT_IN_PROGRESS) {

                System.out.println(
                        "Unexpected saga step: "
                                + saga.getCurrentStep()
                );

                return;
            }

            /*
             * 1. Payment failed
             */
            saga.setCurrentStep(
                    SagaStep.PAYMENT_FAILED
            );

            saga.setUpdatedAt(
                    OffsetDateTime.now()
            );

            sagaInstanceRepository.save(saga);

            /*
             * 2. Start compensation
             */
            saga.setCurrentStep(
                    SagaStep.COMPENSATING
            );

            saga.setUpdatedAt(
                    OffsetDateTime.now()
            );

            saga.setStatus(
                    SagaStatus.IN_PROGRESS
            );

            saga.setCompensationType(
                    CompensationType.PAYMENT_FAILED);

            sagaInstanceRepository.save(saga);

            /*
             * 3. Get seats from Booking DB
             */
            List<BookingSeat> bookingSeats =
                    bookingSeatRepository
                            .findByBookingBookingId(bookingId);

            if (bookingSeats == null
                    || bookingSeats.isEmpty()) {

                throw new IllegalStateException(
                        "No booking seats found for booking: "
                                + bookingId
                );
            }

            List<Long> showSeatIds =
                    bookingSeats.stream()
                            .map(BookingSeat::getShowSeatId)
                            .toList();

            System.out.println(
                    "Seats to release = " + showSeatIds
            );

            /*
             * 4. Create compensation command
             */
            ReleaseSeatsRequestedEvent releaseEvent =
                    ReleaseSeatsRequestedEvent.builder()
                            .eventId(UUID.randomUUID())
                            .sagaId(sagaId)
                            .bookingId(bookingId)
                            .showSeatIds(showSeatIds)
                            .compensationType(
                                    CompensationType.PAYMENT_FAILED
                            )
                            .occurredAt(
                                    OffsetDateTime.now()
                            )
                            .build();

            /*
             * 5. Save to Booking Outbox
             */
            JsonNode payload =
                    objectMapper.valueToTree(
                            releaseEvent
                    );

            OutboxEvent outboxEvent =
                    OutboxEvent.builder()
                            .eventId(
                                    releaseEvent.getEventId()
                            )
                            .aggregateId(bookingId)
                            .aggregateType("BOOKING")
                            .eventType(
                                    "RELEASE_SEATS_REQUESTED"
                            )
                            .payload(payload)
                            .status(
                                    OutboxStatus.PENDING
                            )
                            .retryCount(0)
                            .createdAt(
                                    OffsetDateTime.now()
                            )
                            .build();

            outboxEventRepository.saveAndFlush(
                    outboxEvent
            );

            System.out.println(
                    "RELEASE_SEATS_REQUESTED OUTBOX SAVED = "
                            + releaseEvent.getEventId()
            );

            System.out.println(
                    "Saga moved to COMPENSATING"
            );

            System.out.println(
                    "======================================"
            );
        }
    }


    @Transactional
    public void handleSeatsConfirmed(
            SeatsConfirmedEvent event) {

        System.out.println(
                "===== HANDLE SEATS CONFIRMED ====="
        );

        System.out.println(
                "Saga ID = "
                        + event.getSagaId()
        );

        System.out.println(
                "Booking ID = "
                        + event.getBookingId()
        );
        // 1. FIND SAGA

        SagaInstance saga =
                sagaInstanceRepository
                        .findById(event.getSagaId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Saga not found: "
                                                + event.getSagaId()
                                )
                        );

        // 2. FIND BOOKING

        Booking booking =
                bookingRepository
                        .findById(event.getBookingId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Booking not found: "
                                                + event.getBookingId()
                                )
                        );


        // 3. VALIDATE SAGA STATE

        if (saga.getCurrentStep()
                != SagaStep.CONFIRMING_BOOKING) {

            System.out.println(
                    "Ignoring SEATS_CONFIRMED. "
                            + "Current saga step = "
                            + saga.getCurrentStep()
            );

            return;
        }

        // 4. VALIDATE SEATS

        if (event.getShowSeatIds() == null
                || event.getShowSeatIds().isEmpty()) {

            throw new IllegalStateException(
                    "No confirmed seats received for booking: "
                            + event.getBookingId()
            );
        }


        System.out.println(
                "Confirmed seats = "
                        + event.getShowSeatIds()
        );

        boolean simulateBookingFailure = false;

        try {

            booking.setStatus(
                    BookingStatus.CONFIRMED
            );

            booking.setUpdatedAt(
                    OffsetDateTime.now()
            );

            bookingRepository.saveAndFlush(booking);

            // TEMPORARY FAILURE SIMULATION
            if (simulateBookingFailure) {

                throw new RuntimeException(
                        "SIMULATED BOOKING CONFIRMATION FAILURE"
                );
            }

            System.out.println(
                    "Booking transitioned to CONFIRMED"
            );

            // 6. SAGA → COMPLETED

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

            sagaInstanceRepository.saveAndFlush(saga);

            System.out.println(
                    "Saga transitioned to COMPLETED"
            );

        } catch (Exception ex) {

            System.out.println(
                    "===== BOOKING CONFIRMATION FAILED ====="
            );

            System.out.println(
                    "Reason = " + ex.getMessage()
            );

            // Mark booking as FAILED

            booking.setStatus(
                    BookingStatus.FAILED
            );

            booking.setUpdatedAt(
                    OffsetDateTime.now()
            );

            bookingRepository.saveAndFlush(booking);

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

            sagaInstanceRepository.saveAndFlush(saga);

            System.out.println(
                    "Saga moved to COMPENSATING"
            );

            // Compensation

            compensateAfterBookingFailure(
                    saga,
                    booking,
                    bookingSeatRepository
                            .findByBookingBookingId(
                                    booking.getBookingId()
                            )
            );
        }
    }

    @Override
    @Transactional
    public void handleSeatsReleased(
            SeatsReleasedEvent event) {

        System.out.println(
                "========== SEATS RELEASED EVENT RECEIVED =========="
        );

        System.out.println(
                "Saga ID = " + event.getSagaId()
        );

        System.out.println(
                "Booking ID = " + event.getBookingId()
        );

        System.out.println(
                "Compensation Type = " + event.getCompensationType()
        );

        SagaInstance saga =
                sagaInstanceRepository
                        .findById(event.getSagaId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Saga not found: "
                                                + event.getSagaId()
                                )
                        );

        System.out.println(
                "Current Saga Step = "
                        + saga.getCurrentStep()
        );

        System.out.println(
                "Saga Status = "
                        + saga.getStatus()
        );

        // IDEMPOTENCY

        if (saga.getStatus() != SagaStatus.IN_PROGRESS) {

            System.out.println(
                    "Ignoring SEATS_RELEASED event because "
                            + "Saga is no longer in progress."
            );

            return;
        }


        // EXPECTED STATE

        if (saga.getCurrentStep() != SagaStep.COMPENSATING) {

            System.out.println(
                    "Ignoring SEATS_RELEASED. Current step = "
                            + saga.getCurrentStep()
            );

            return;
        }

        // CASE 1: PAYMENT FAILED
        if (event.getCompensationType()
                == CompensationType.PAYMENT_FAILED) {

            System.out.println(
                    "Payment failed -> seats released successfully"
            );

            /*
             * Payment was NEVER successful.
             *
             * Therefore:
             *
             * NO REFUND
             *
             * Compensation is complete.
             */

            markSagaCompensationCompleted(saga);

            System.out.println(
                    "Saga marked FAILED after PAYMENT_FAILED compensation"
            );

            System.out.println(
                    "========== PAYMENT FAILURE COMPENSATION COMPLETE =========="
            );

            return;
        }

        // CASE 2: BOOKING FAILED AFTER PAYMENT




        if (event.getCompensationType()
                == CompensationType.BOOKING_FAILED_AFTER_PAYMENT) {

            System.out.println(
                    "Booking failed after payment -> refund required"
            );

            // MOVE TO REFUND_PENDING

            saga.setCurrentStep(
                    SagaStep.REFUND_PENDING
            );

            saga.setUpdatedAt(
                    OffsetDateTime.now()
            );

            sagaInstanceRepository.save(saga);

            // CREATE REFUND_REQUESTED EVENT

            RefundRequestedEvent refundEvent =
                    RefundRequestedEvent.builder()
                            .eventId(UUID.randomUUID())
                            .sagaId(event.getSagaId())
                            .bookingId(event.getBookingId())
                            .occurredAt(
                                    OffsetDateTime.now()
                            )
                            .build();

            // SAVE REFUND_REQUESTED TO OUTBOX

            OutboxEvent outboxEvent =
                    OutboxEvent.builder()
                            .eventId(
                                    refundEvent.getEventId()
                            )
                            .eventType(
                                    "REFUND_REQUESTED"
                            )
                            .aggregateType(
                                    "BOOKING"
                            )
                            .aggregateId(
                                    refundEvent.getBookingId()
                            )
                            .payload(
                                    objectMapper.valueToTree(
                                            refundEvent
                                    )
                            )
                            .status(
                                    OutboxStatus.PENDING
                            )
                            .retryCount(0)
                            .createdAt(
                                    OffsetDateTime.now()
                            )
                            .build();

            outboxEventRepository.saveAndFlush(
                    outboxEvent
            );

            System.out.println(
                    "REFUND_REQUESTED OUTBOX SAVED = "
                            + refundEvent.getEventId()
            );

            System.out.println(
                    "Saga moved to REFUND_PENDING"
            );

            System.out.println(
                    "========== REFUND REQUESTED =========="
            );

            return;
        }

        // UNKNOWN COMPENSATION TYPE

        throw new IllegalStateException(
                "Unknown compensation type: "
                        + event.getCompensationType()
        );
    }

    @Override
    @Transactional
    public void processRefund(RefundRequestedEvent event) {

        System.out.println(
                "========== PROCESS REFUND =========="
        );

        System.out.println(
                "Saga ID = " + event.getSagaId()
        );

        System.out.println(
                "Booking ID = " + event.getBookingId()
        );

        SagaInstance saga =
                sagaInstanceRepository
                        .findById(event.getSagaId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Saga not found: "
                                                + event.getSagaId()
                                )
                        );

        // IDEMPOTENCY

        if (saga.getStatus() != SagaStatus.IN_PROGRESS) {

            System.out.println(
                    "Ignoring REFUND_REQUESTED because saga is "
                            + saga.getStatus()
            );

            return;
        }

        if (saga.getCurrentStep() != SagaStep.REFUND_PENDING) {

            System.out.println(
                    "Ignoring REFUND_REQUESTED. Current step = "
                            + saga.getCurrentStep()
            );

            return;
        }

        // CHECK EXISTING REFUND

        Optional<Refund> existingRefund =
               refundRepository.findByBookingId(
                        event.getBookingId()
                );

        RefundResponse refundResponse;

        // FIRST REFUND REQUEST

        if (existingRefund.isEmpty()) {

            System.out.println(
                    "===== FIRST REFUND REQUEST ====="
            );

            refundResponse =
                    paymentService.refundPayment(
                            event.getBookingId()
                    );

        }
        // RETRY EXISTING REFUND

        else {

            Refund refund = existingRefund.get();

            System.out.println(
                    "===== EXISTING REFUND FOUND ====="
            );

            System.out.println(
                    "Refund ID = "
                            + refund.getRefundId()
            );

            System.out.println(
                    "Refund Status = "
                            + refund.getStatus()
            );

            // Already successful
            if (refund.getStatus() == RefundStatus.SUCCESS) {

                refundResponse =
                        RefundResponse.builder()
                                .refundId(
                                        refund.getRefundId()
                                )
                                .bookingId(
                                        refund.getBookingId()
                                )
                                .amount(
                                        refund.getAmount()
                                )
                                .status(
                                        RefundStatus.SUCCESS
                                )
                                .build();
            }

            // Retry pending refund
            else if (refund.getStatus()
                    == RefundStatus.PENDING) {

                refundResponse =
                        paymentService.processPendingRefund(
                                event.getBookingId()
                        );
            }

            else {

                throw new IllegalStateException(
                        "Unexpected refund status: "
                                + refund.getStatus()
                );
            }
        }

        System.out.println(
                "Refund ID = "
                        + refundResponse.getRefundId()
        );

        System.out.println(
                "Refund Status = "
                        + refundResponse.getStatus()
        );

        // REFUND SUCCESS

        if (refundResponse.getStatus()
                == RefundStatus.SUCCESS) {

            RefundSucceededEvent succeededEvent =
                    RefundSucceededEvent.builder()
                            .eventId(UUID.randomUUID())
                            .sagaId(event.getSagaId())
                            .bookingId(event.getBookingId())
                            .occurredAt(
                                    OffsetDateTime.now()
                            )
                            .build();

            OutboxEvent outboxEvent =
                    OutboxEvent.builder()
                            .eventId(
                                    succeededEvent.getEventId()
                            )
                            .eventType(
                                    "REFUND_SUCCEEDED"
                            )
                            .aggregateType(
                                    "REFUND"
                            )
                            .aggregateId(
                                    event.getBookingId()
                            )
                            .payload(
                                    objectMapper.valueToTree(
                                            succeededEvent
                                    )
                            )
                            .status(
                                    OutboxStatus.PENDING
                            )
                            .retryCount(0)
                            .createdAt(
                                    OffsetDateTime.now()
                            )
                            .build();

            outboxEventRepository.save(
                    outboxEvent
            );

            System.out.println(
                    "REFUND_SUCCEEDED OUTBOX SAVED = "
                            + succeededEvent.getEventId()
            );

            return;
        }

        // REFUND STILL PENDING

        if (refundResponse.getStatus()
                == RefundStatus.PENDING) {

            saga.setCurrentStep(
                    SagaStep.REFUND_PENDING
            );

            saga.setLastError(
                    "Refund pending. Waiting for retry."
            );

            saga.setUpdatedAt(
                    OffsetDateTime.now()
            );

            sagaInstanceRepository.save(saga);

            RefundPendingEvent pendingEvent =
                    RefundPendingEvent.builder()
                            .eventId(UUID.randomUUID())
                            .sagaId(event.getSagaId())
                            .bookingId(event.getBookingId())
                            .occurredAt(
                                    OffsetDateTime.now()
                            )
                            .build();

            OutboxEvent outboxEvent =
                    OutboxEvent.builder()
                            .eventId(
                                    pendingEvent.getEventId()
                            )
                            .eventType(
                                    "REFUND_PENDING"
                            )
                            .aggregateType(
                                    "REFUND"
                            )
                            .aggregateId(
                                    event.getBookingId()
                            )
                            .payload(
                                    objectMapper.valueToTree(
                                            pendingEvent
                                    )
                            )
                            .status(
                                    OutboxStatus.PENDING
                            )
                            .retryCount(0)
                            .createdAt(
                                    OffsetDateTime.now()
                            )
                            .build();

            outboxEventRepository.save(
                    outboxEvent
            );

            System.out.println(
                    "REFUND_PENDING OUTBOX SAVED = "
                            + pendingEvent.getEventId()
            );

            return;
        }

        throw new IllegalStateException(
                "Unexpected refund status: "
                        + refundResponse.getStatus()
        );
    }


    // COMPENSATE / RECOVER SAGA
    @Override
    @Transactional
    public void retryCompensation(UUID sagaId) {

        System.out.println("inside retry compensation");

        SagaInstance saga = sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Saga not found: " + sagaId));

        if (saga.getStatus() != SagaStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Saga is not eligible for compensation retry. Status = "
                            + saga.getStatus()
            );
        }

        if (saga.getCurrentStep() != SagaStep.COMPENSATING
                && saga.getCurrentStep() != SagaStep.REFUND_PENDING) {

            throw new IllegalStateException(
                    "Saga is not waiting for compensation. Current step = "
                            + saga.getCurrentStep()
            );
        }

        Booking booking = bookingRepository.findById(saga.getBookingId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Booking not found: " + saga.getBookingId()
                        ));

          // CASE 1: SEAT COMPENSATION
        System.out.println("inside retry compensation before seat compensation");
        if (saga.getCurrentStep() == SagaStep.COMPENSATING) {

            List<BookingSeat> bookingSeats =
                    bookingSeatRepository.findByBookingBookingId(booking.getBookingId());

            List<Long> showSeatIds = bookingSeats.stream()
                    .map(BookingSeat::getShowSeatId)
                    .toList();

            if (showSeatIds.isEmpty()) {
                throw new IllegalStateException(
                        "No booking seats found for booking: "
                                + booking.getBookingId()
                );
            }

            ReleaseSeatsRequestedEvent event =
                    ReleaseSeatsRequestedEvent.builder()
                            .eventId(UUID.randomUUID())
                            .sagaId(saga.getSagaId())
                            .bookingId(booking.getBookingId())
                            .showSeatIds(showSeatIds)
                            .compensationType(saga.getCompensationType())
                            .occurredAt(OffsetDateTime.now())
                            .build();

            JsonNode payload =
                    objectMapper.valueToTree(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(event.getEventId())
                    .aggregateType("BOOKING")
                    .aggregateId(booking.getBookingId())
                    .eventType("RELEASE_SEATS_REQUESTED")
                    .payload(payload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .createdAt(OffsetDateTime.now())
                    .build();

            outboxEventRepository.save(outboxEvent);

            System.out.println(
                    "========== COMPENSATION RETRY =========="
            );

            System.out.println(
                    "RELEASE_SEATS_REQUESTED OUTBOX CREATED"
            );

            return;
        }


         // CASE 2: REFUND RETRY
        if (saga.getCurrentStep() == SagaStep.REFUND_PENDING) {

            RefundRequestedEvent event =
                    RefundRequestedEvent.builder()
                            .eventId(UUID.randomUUID())
                            .sagaId(saga.getSagaId())
                            .bookingId(booking.getBookingId())
                            //.amount(booking.getSubtotal())
                           // .reason("Booking compensation")
                            .occurredAt(OffsetDateTime.now())
                            .build();

            JsonNode payload =
                    objectMapper.valueToTree(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(event.getEventId())
                    .aggregateType("REFUND")
                    .aggregateId(booking.getBookingId())
                    .eventType("REFUND_REQUESTED")
                    .payload(payload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .createdAt(OffsetDateTime.now())
                    .build();

            outboxEventRepository.save(outboxEvent);

            System.out.println(
                    "========== REFUND RETRY =========="
            );

            System.out.println(
                    "REFUND_REQUESTED OUTBOX CREATED"
            );
        }
    }

    @Override
    @Transactional
    public void handleRefundSucceeded(
            RefundSucceededEvent event) {

        System.out.println(
                "========== HANDLE REFUND SUCCEEDED =========="
        );

        SagaInstance saga =
                sagaInstanceRepository
                        .findById(event.getSagaId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Saga not found: "
                                                + event.getSagaId()
                                )
                        );

        // IDEMPOTENCY

        if (saga.getStatus() != SagaStatus.IN_PROGRESS) {

            System.out.println(
                    "Ignoring REFUND_SUCCEEDED. Saga status = "
                            + saga.getStatus()
            );

            return;
        }

        // EXPECTED STATE

        if (saga.getCurrentStep()
                != SagaStep.REFUND_PENDING) {

            System.out.println(
                    "Ignoring REFUND_SUCCEEDED. Current step = "
                            + saga.getCurrentStep()
            );

            return;
        }

        // FIND BOOKING

        Booking booking =
                bookingRepository
                        .findById(event.getBookingId())
                        .orElseThrow(() ->
                                new BookingNotFoundException(
                                        event.getBookingId()
                                )
                        );

        // CANCEL BOOKING

        booking.setStatus(
                BookingStatus.CANCELLED
        );

        booking.setUpdatedAt(
                OffsetDateTime.now()
        );

        bookingRepository.save(booking);

        // COMPENSATION COMPLETED

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

        System.out.println(
                "========== COMPENSATION COMPLETED =========="
        );

        System.out.println(
                "Saga ID = " + saga.getSagaId()
        );

        System.out.println(
                "Booking ID = " + booking.getBookingId()
        );

        System.out.println(
                "Booking Status = " + booking.getStatus()
        );

        System.out.println(
                "Saga Status = " + saga.getStatus()
        );

        System.out.println(
                "Saga Step = " + saga.getCurrentStep()
        );
    }


    private void compensateAfterBookingFailure(
            SagaInstance saga,
            Booking booking,
            List<BookingSeat> bookingSeats) {

        try {

            List<Long> showSeatIds =
                    bookingSeats.stream()
                            .map(BookingSeat::getShowSeatId)
                            .toList();

            ReleaseSeatsRequestedEvent event =
                    ReleaseSeatsRequestedEvent.builder()
                            .eventId(UUID.randomUUID())
                            .sagaId(saga.getSagaId())
                            .bookingId(saga.getBookingId())
                            .showSeatIds(showSeatIds)
                            .occurredAt(OffsetDateTime.now())
                            .compensationType(CompensationType.BOOKING_FAILED_AFTER_PAYMENT)
                            .build();

            OutboxEvent outboxEvent =
                    OutboxEvent.builder()
                            .eventId(event.getEventId())
                            .aggregateType("RELEASE_SEATS_REQUESTED")
                            .eventType("RELEASE_SEATS_REQUESTED")
                            .aggregateId(
                                    event.getBookingId()
                            )
                            .payload(
                                    objectMapper.valueToTree(event)
                            )
                            .status(OutboxStatus.PENDING)
                            .retryCount(0)
                            .createdAt(OffsetDateTime.now())
                            .build();

            outboxEventRepository.save(outboxEvent);

            System.out.println("out box event for release seat is called");

            // 2. REFUND PAYMENT





        } catch (Exception ex) {

            sagaStateService.markCompensationFailed(
                    saga.getSagaId(),
                    ex
            );

            throw ex;
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
         * Therefore, Saga is finished.
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

