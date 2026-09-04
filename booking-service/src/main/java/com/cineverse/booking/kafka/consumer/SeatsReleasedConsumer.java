package com.cineverse.booking.kafka.consumer;

import com.cineverse.booking.kafka.dtos.SeatsConfirmedEvent;
import com.cineverse.booking.kafka.dtos.SeatsReleasedEvent;
import com.cineverse.booking.sagaServices.BookingSagaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class SeatsReleasedConsumer {

    private final BookingSagaOrchestrator bookingSagaOrchestrator;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "inventory.seats-released",
            groupId = "booking-service"
    )
    public void consume(String message) {

        try {

            System.out.println(
                    "========== SEATS RELEASED EVENT RECEIVED =========="
            );

            System.out.println(
                    "RAW MESSAGE = " + message
            );

            SeatsReleasedEvent event =
                    objectMapper.readValue(
                            message,
                            SeatsReleasedEvent.class
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
                    "Show Seats = " + event.getShowSeatIds()
            );

            bookingSagaOrchestrator.handleSeatsReleased(event);

        } catch (Exception e) {

            System.out.println(
                    "FAILED TO PROCESS SEATS_RELEASED EVENT"
            );

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to process SEATS_RELEASED event",
                    e
            );
        }
    }
}
