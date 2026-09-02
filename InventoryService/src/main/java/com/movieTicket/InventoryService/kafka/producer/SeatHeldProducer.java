package com.movieTicket.InventoryService.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieTicket.InventoryService.kafka.dtos.SeatHeldEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeatHeldProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private static final String TOPIC = "inventory.seat-held";

    public void publish(SeatHeldEvent event) {

        try {

            String message =
                    objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    TOPIC,
                    event.getBookingId().toString(),
                    message
            );

            System.out.println(
                    "SEAT_HELD EVENT PUBLISHED = " + message
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to publish SEAT_HELD event",
                    e
            );
        }
    }
}
