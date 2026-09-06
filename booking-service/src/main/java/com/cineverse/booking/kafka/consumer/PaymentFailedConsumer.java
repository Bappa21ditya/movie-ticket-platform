package com.cineverse.booking.kafka.consumer;

import com.cineverse.booking.kafka.dtos.PaymentFailedEvent;
import com.cineverse.booking.kafka.dtos.PaymentSucceededEvent;
import com.cineverse.booking.sagaServices.BookingSagaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFailedConsumer {
    private final ObjectMapper objectMapper;

    private final BookingSagaOrchestrator bookingSagaOrchestrator;

    @KafkaListener(
            topics = "booking.payment.failed",
            groupId = "booking-payment-result-group"
    )
    public void consume(String message) {

        System.out.println(
                "========== PAYMENT_FAILED RECEIVED =========="
        );

        System.out.println("Payload = " + message);

        try {

            PaymentFailedEvent event =
                    objectMapper.readValue(
                            message,
                            PaymentFailedEvent.class
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
                    "Payment ID = " + event.getPaymentId()
            );

            bookingSagaOrchestrator.handlePaymentFailure(event);

            System.out.println(
                    "PAYMENT_FAILURE HANDLED"
            );

        } catch (Exception ex) {

            System.err.println(
                    "FAILED TO PROCESS PAYMENT_FAILED"
            );

            ex.printStackTrace();

            throw new RuntimeException(
                    "Payment failed event processing failed",
                    ex
            );
        }
    }
}
