package com.cineverse.booking.kafka.consumer;


import com.cineverse.booking.kafka.dtos.SeatHeldEvent;
import com.cineverse.booking.sagaServices.BookingSagaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeatHeldConsumer {

    private final ObjectMapper objectMapper;
    private final BookingSagaOrchestrator bookingSagaOrchestrator;

    @KafkaListener(
            topics = "inventory.seat-held",
            groupId = "booking-seat-held-group"
    )
    public void consume(String message) {

        try {

            System.out.println(
                    "========== SEAT_HELD RECEIVED =========="
            );

            System.out.println(
                    "Payload = " + message
            );

            SeatHeldEvent event =
                    objectMapper.readValue(
                            message,
                            SeatHeldEvent.class
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
                    "Hold ID = " + event.getHoldId()
            );

            bookingSagaOrchestrator.handleSeatHeld(
                    event
            );

        } catch (Exception e) {

            System.err.println(
                    "FAILED TO PROCESS SEAT_HELD"
            );

            e.printStackTrace();

            throw new RuntimeException(
                    "SEAT_HELD processing failed",
                    e
            );
        }
    }
}
