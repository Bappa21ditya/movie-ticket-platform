package com.cineverse.booking.kafka.consumer;


import com.cineverse.booking.kafka.dtos.SeatsConfirmedEvent;
import com.cineverse.booking.sagaServices.BookingSagaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeatsConfirmedConsumer {
    private final ObjectMapper objectMapper;

    private final BookingSagaOrchestrator sagaOrchestrator;


    @PostConstruct
    public void init() {

        System.out.println(
                "===== SEATS CONFIRMED KAFKA CONSUMER INITIALIZED ====="
        );
    }


    @KafkaListener(
            topics = "inventory.seats-confirmed",
            groupId = "booking-seats-confirmed-group-v1"
    )
    public void consume(String message) {

        System.out.println(
                "========== SEATS_CONFIRMED EVENT RECEIVED =========="
        );

        System.out.println(
                "Payload = " + message
        );

        try {

            // 1. DESERIALIZE EVENT

            SeatsConfirmedEvent event =
                    objectMapper.readValue(
                            message,
                            SeatsConfirmedEvent.class
                    );

            // 2. LOG EVENT

            System.out.println(
                    "Event ID = "
                            + event.getEventId()
            );

            System.out.println(
                    "Saga ID = "
                            + event.getSagaId()
            );

            System.out.println(
                    "Booking ID = "
                            + event.getBookingId()
            );

            System.out.println(
                    "ShowSeat IDs = "
                            + event.getShowSeatIds()
            );

            // 3. HANDLE EVENT

            sagaOrchestrator.handleSeatsConfirmed(event);


            System.out.println(
                    "===================================================="
            );


        } catch (Exception ex) {

            System.err.println(
                    "FAILED TO PROCESS SEATS_CONFIRMED"
            );

            ex.printStackTrace();

            throw new RuntimeException(
                    "Seats confirmed event processing failed",
                    ex
            );
        }
    }

}
