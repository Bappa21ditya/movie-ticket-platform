package com.movieTicket.InventoryService.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieTicket.InventoryService.enums.CompensationType;
import com.movieTicket.InventoryService.kafka.dtos.ReleaseSeatsRequestedEvent;
import com.movieTicket.InventoryService.kafka.dtos.SeatsReleasedEvent;
import com.movieTicket.InventoryService.kafka.producer.SeatsReleasedProducer;
import com.movieTicket.InventoryService.services.SeatHoldService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReleaseSeatsRequestedConsumer {
    private final ObjectMapper objectMapper;

    private final SeatHoldService seatHoldService;

    private final SeatsReleasedProducer seatsReleasedProducer;

    @PostConstruct
    public void init() {

        System.out.println(
                "===== RELEASE SEATS REQUESTED KAFKA CONSUMER INITIALIZED ====="
        );
    }

    @KafkaListener(
            topics = "release-seats-requested",
            groupId = "inventory-seat-release-group-v1"
    )
    public void consume(String message) {

        System.out.println(
                "========== RELEASE_SEATS_REQUESTED RECEIVED =========="
        );

        System.out.println(
                "Payload = " + message
        );

        try {

            ReleaseSeatsRequestedEvent event =
                    objectMapper.readValue(
                            message,
                            ReleaseSeatsRequestedEvent.class
                    );

            System.out.println(
                    "Event ID = " + event.getEventId()
            );

            System.out.println(
                    "Saga ID = " + event.getSagaId()
            );

            System.out.println(
                    "Booking ID = " + event.getBookingId()
            );

            System.out.println(
                    "ShowSeat IDs = " + event.getShowSeatIds()
            );

            System.out.println(
                    "Compensation Type = "
                            + event.getCompensationType()
            );


            // VALIDATION

            if (event.getBookingId() == null) {

                throw new IllegalArgumentException(
                        "Booking ID cannot be null"
                );
            }

            if (event.getShowSeatIds() == null
                    || event.getShowSeatIds().isEmpty()) {

                throw new IllegalArgumentException(
                        "ShowSeat IDs cannot be empty"
                );
            }

            if (event.getCompensationType() == null) {

                throw new IllegalArgumentException(
                        "Compensation type cannot be null"
                );
            }

            // RELEASE SEATS

            for (Long showSeatId :
                    event.getShowSeatIds()) {

                System.out.println(
                        "Processing ShowSeat ID = "
                                + showSeatId
                );

                // PAYMENT FAILURE

                if (event.getCompensationType()
                        == CompensationType.PAYMENT_FAILED) {

                    System.out.println(
                            "Payment failed compensation"
                    );

                    System.out.println(
                            "Releasing HELD seat = "
                                    + showSeatId
                    );

                    seatHoldService.releaseHold(
                            showSeatId,
                            event.getBookingId()
                    );
                }


                // BOOKING FAILURE AFTER PAYMENT

                else if (event.getCompensationType()
                        == CompensationType.BOOKING_FAILED_AFTER_PAYMENT) {

                    System.out.println(
                            "Booking failure after payment compensation"
                    );

                    System.out.println(
                            "Releasing BOOKED seat = "
                                    + showSeatId
                    );

                    seatHoldService.compensateConfirmedSeat(
                            showSeatId,
                            event.getBookingId()
                    );
                }


                // UNKNOWN COMPENSATION TYPE

                else {

                    throw new IllegalArgumentException(
                            "Unsupported compensation type: "
                                    + event.getCompensationType()
                    );
                }

                System.out.println(
                        "ShowSeat RELEASED = "
                                + showSeatId
                );
            }


            System.out.println(
                    "ALL SEATS RELEASED"
            );

            // CREATE SEATS RELEASED EVENT

            SeatsReleasedEvent releasedEvent =
                    SeatsReleasedEvent.builder()
                            .eventId(
                                    UUID.randomUUID()
                            )
                            .sagaId(
                                    event.getSagaId()
                            )
                            .bookingId(
                                    event.getBookingId()
                            )
                            .showSeatIds(
                                    event.getShowSeatIds()
                            )
                            .compensationType(
                                    event.getCompensationType()
                            )
                            .occurredAt(
                                    OffsetDateTime.now()
                            )
                            .build();

            // PUBLISH RESULT EVENT

            seatsReleasedProducer.publish(
                    releasedEvent
            );


            System.out.println(
                    "SEATS_RELEASED EVENT PUBLISHED"
            );

        } catch (Exception ex) {

            System.err.println(
                    "FAILED TO PROCESS RELEASE_SEATS_REQUESTED"
            );

            ex.printStackTrace();

            throw new RuntimeException(
                    "Seat release processing failed",
                    ex
            );
        }
    }
}
