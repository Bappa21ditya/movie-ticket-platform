package com.movieTicket.InventoryService.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieTicket.InventoryService.dtos.CreateSeatHoldRequest;
import com.movieTicket.InventoryService.dtos.SeatHoldResponse;
import com.movieTicket.InventoryService.kafka.dtos.SeatHeldEvent;
import com.movieTicket.InventoryService.kafka.dtos.SeatHoldRequestedEvent;
import com.movieTicket.InventoryService.kafka.producer.SeatHeldProducer;
import com.movieTicket.InventoryService.services.SeatHoldService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeatHoldRequestedConsumer {

    private final ObjectMapper objectMapper;
    private final SeatHoldService seatHoldService;
    private final SeatHeldProducer seatHeldProducer;


    @PostConstruct
    public void init() {
        System.out.println(
                "===== SEAT HOLD KAFKA CONSUMER INITIALIZED ====="
        );
    }

    @KafkaListener(
            topics = "booking.seat-hold",
            groupId = "inventory-seat-hold-group-v2"
    )
    public void consume(String message) {

        System.out.println(
                "========== SEAT HOLD EVENT RECEIVED =========="
        );

        System.out.println(
                "Payload = " + message
        );

        try {

            OffsetDateTime test =
                    objectMapper.readValue(
                            "\"2026-09-01T17:49:28.132742+05:30\"",
                            OffsetDateTime.class
                    );

            System.out.println(
                    "OFFSET DATE TEST = " + test
            );

            SeatHoldRequestedEvent event =
                    objectMapper.readValue(
                            message,
                            SeatHoldRequestedEvent.class
                    );

            System.out.println(
                    "Event ID = " + event.getEventId()
            );

            System.out.println(
                    "Booking ID = " + event.getBookingId()
            );

            System.out.println(
                    "ShowSeat ID = " + event.getShowSeatId()
            );

            CreateSeatHoldRequest request =
                    CreateSeatHoldRequest.builder()
                            .showSeatId(event.getShowSeatId())
                            .bookingId(event.getBookingId())
                            .userId(event.getUserId())
                            .expiresAt(event.getExpiresAt())
                            .build();

            SeatHoldResponse response =
                    seatHoldService.createHold(request);

            System.out.println(
                    "SEAT HOLD CREATED = "
                            + response.getHoldId()
            );

            System.out.println(
                    "=============================================="
            );

            SeatHeldEvent seatHeldEvent =
                    SeatHeldEvent.builder()
                            .eventId(UUID.randomUUID())
                            .sagaId(event.getSagaId())
                            .bookingId(event.getBookingId())
                            .userId(event.getUserId())
                            .showSeatId(event.getShowSeatId())
                            .holdId(response.getHoldId())
                            .expiresAt(event.getExpiresAt())
                            .occurredAt(OffsetDateTime.now())
                            .build();

            seatHeldProducer.publish(seatHeldEvent);

        } catch (Exception ex) {

            System.err.println(
                    "FAILED TO PROCESS SEAT_HOLD_REQUESTED"
            );

            ex.printStackTrace();

            throw new RuntimeException(
                    "Seat hold event processing failed",
                    ex
            );
        }
    }
}
