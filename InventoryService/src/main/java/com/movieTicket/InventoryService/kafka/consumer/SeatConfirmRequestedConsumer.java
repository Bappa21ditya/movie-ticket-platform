package com.movieTicket.InventoryService.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieTicket.InventoryService.kafka.dtos.SeatConfirmRequestedEvent;
import com.movieTicket.InventoryService.kafka.dtos.SeatsConfirmedEvent;
import com.movieTicket.InventoryService.kafka.producer.SeatConfirmRequestedProducer;
import com.movieTicket.InventoryService.services.SeatHoldService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeatConfirmRequestedConsumer {


    private final ObjectMapper objectMapper;
    private final SeatHoldService seatHoldService;
    private final SeatConfirmRequestedProducer seatConfirmedProducer;

    @PostConstruct
    public void init() {
        System.out.println(
                "===== SEAT CONFIRM REQUESTED KAFKA CONSUMER INITIALIZED ====="
        );
    }

    @KafkaListener(
            topics = "booking.seat-confirm",
            groupId = "inventory-seat-confirm-group-v1"
    )
    public void consume(String message) {

        System.out.println(
                "========== SEAT CONFIRM REQUEST RECEIVED =========="
        );

        System.out.println(
                "Payload = " + message
        );

        try {

            // 1. DESERIALIZE EVENT

            SeatConfirmRequestedEvent event =
                    objectMapper.readValue(
                            message,
                            SeatConfirmRequestedEvent.class
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


            // 2. VALIDATE

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


            // 3. CONFIRM EACH SEAT

            for (Long showSeatId : event.getShowSeatIds()) {

                System.out.println(
                        "Confirming ShowSeat ID = "
                                + showSeatId
                );

                seatHoldService.confirmSeat(
                        showSeatId,
                        event.getBookingId()

                );

                System.out.println(
                        "ShowSeat CONFIRMED = "
                                + showSeatId
                );
            }

            // 4. ALL SEATS CONFIRMED

            System.out.println(
                    "ALL SEATS CONFIRMED"
            );


            // 5. CREATE RESPONSE EVENT

            SeatsConfirmedEvent seatsConfirmedEvent =
                    SeatsConfirmedEvent.builder()
                            .eventId(UUID.randomUUID())
                            .sagaId(event.getSagaId())
                            .bookingId(event.getBookingId())
                            .showSeatIds(event.getShowSeatIds())
                            .occurredAt(OffsetDateTime.now())
                            .build();

            // 6. PUBLISH RESPONSE THROUGH OUTBOX

            seatConfirmedProducer.publish(
                    seatsConfirmedEvent
            );


            System.out.println(
                    "SEATS_CONFIRMED EVENT PUBLISHED"
            );

            System.out.println(
                    "=================================================="
            );

        } catch (Exception ex) {

            System.err.println(
                    "FAILED TO PROCESS SEAT_CONFIRM_REQUESTED"
            );

            ex.printStackTrace();

            throw new RuntimeException(
                    "Seat confirmation event processing failed",
                    ex
            );
        }
    }
}
