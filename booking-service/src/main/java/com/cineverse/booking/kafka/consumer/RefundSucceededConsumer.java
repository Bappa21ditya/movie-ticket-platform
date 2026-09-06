package com.cineverse.booking.kafka.consumer;


import com.cineverse.booking.kafka.dtos.RefundSucceededEvent;
import com.cineverse.booking.sagaServices.BookingSagaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundSucceededConsumer {
    private final ObjectMapper objectMapper;
    private final BookingSagaOrchestrator bookingSagaOrchestrator;

    @KafkaListener(
            topics = "payment.refund-succeeded",
            groupId = "booking-refund-result-group"
    )
    public void consume(String message) {

        try {

            System.out.println(
                    "========== REFUND SUCCEEDED RECEIVED =========="
            );

            RefundSucceededEvent event =
                    objectMapper.readValue(
                            message,
                            RefundSucceededEvent.class
                    );

            System.out.println(
                    "Saga ID = " + event.getSagaId()
            );

            System.out.println(
                    "Booking ID = " + event.getBookingId()
            );

            bookingSagaOrchestrator.handleRefundSucceeded(event);

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to process REFUND_SUCCEEDED",
                    e
            );
        }
    }
}
