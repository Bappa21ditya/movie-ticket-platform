package com.movieTicket.InventoryService.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieTicket.InventoryService.kafka.dtos.ReleaseSeatsRequestedEvent;
import com.movieTicket.InventoryService.kafka.dtos.SeatsReleasedEvent;
import com.movieTicket.InventoryService.kafka.producer.SeatsReleasedProducer;
import com.movieTicket.InventoryService.services.SeatHoldService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReleaseSeatsRequestedConsumer {

    private final SeatHoldService seatHoldService;

     private final SeatsReleasedProducer seatsReleasedProducer;

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "release-seats-requested",
            groupId = "inventory-service"
    )
    public void consume(
            String message) {

        try {

            System.out.println(
                    "========== RELEASE SEATS REQUESTED EVENT RECEIVED =========="
            );

            System.out.println(
                    "RAW MESSAGE = " + message
            );

            ReleaseSeatsRequestedEvent event =
                    objectMapper.readValue(
                            message,
                            ReleaseSeatsRequestedEvent.class
                    );

            System.out.println(
                    "Saga ID = " + event.getSagaId()
            );

            System.out.println(
                    "Booking ID = " + event.getBookingId()
            );

            System.out.println(
                    "Show Seat IDs = " + event.getShowSeatIds()
            );

            // Release all confirmed seats
            for (Long showSeatId : event.getShowSeatIds()) {

                seatHoldService.compensateConfirmedSeat(
                        showSeatId,
                        event.getBookingId()
                );
            }

            System.out.println(
                    "ALL CONFIRMED SEATS RELEASED"
            );

            // Create response event
            SeatsReleasedEvent releasedEvent =
                    SeatsReleasedEvent.builder()
                            .eventId(UUID.randomUUID())
                            .sagaId(event.getSagaId())
                            .bookingId(event.getBookingId())
                            .showSeatIds(event.getShowSeatIds())
                            .occurredAt(OffsetDateTime.now())
                            .build();

            // Publish response
            seatsReleasedProducer.publish(
                    releasedEvent
            );

            System.out.println(
                    "SEATS_RELEASED EVENT PUBLISHED"
            );

        } catch (Exception e) {

            System.out.println(
                    "FAILED TO PROCESS RELEASE_SEATS_REQUESTED"
            );

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to process release seats event",
                    e
            );
        }
    }
}
