package com.movieTicket.InventoryService.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieTicket.InventoryService.kafka.dtos.SeatConfirmRequestedEvent;
import com.movieTicket.InventoryService.kafka.dtos.SeatsConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeatConfirmRequestedProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private static final String TOPIC = "inventory.seats-confirmed";

    public void publish(SeatsConfirmedEvent event) {

        try {

            String message =
                    objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    TOPIC,
                    event.getBookingId().toString(),
                    message
            );

            System.out.println(
                    "SEAT_CONFIRM_REQUESTED EVENT PUBLISHED = "
                            + message
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to publish SEAT_CONFIRM_REQUESTED event",
                    e
            );
        }
    }
}
