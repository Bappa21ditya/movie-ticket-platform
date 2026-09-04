package com.movieTicket.InventoryService.kafka.producer;


import com.movieTicket.InventoryService.kafka.dtos.ReleaseSeatsRequestedEvent;
import com.movieTicket.InventoryService.kafka.dtos.SeatsReleasedEvent;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieTicket.InventoryService.kafka.dtos.SeatConfirmRequestedEvent;
import com.movieTicket.InventoryService.kafka.dtos.SeatsConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class SeatsReleasedProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private static final String TOPIC =
            "inventory.seats-released";

    public void publish(SeatsReleasedEvent event) {

        try {

            String message =
                    objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    TOPIC,
                    event.getBookingId().toString(),
                    message
            );

            System.out.println(
                    "SEATS_RELEASED EVENT PUBLISHED = "
                            + message
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to publish SEATS_RELEASED event",
                    e
            );
        }
    }
}
