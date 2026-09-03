package com.cineverse.booking.kafka.consumer;


import com.cineverse.booking.kafka.dtos.PaymentSucceededEvent;
import com.cineverse.booking.sagaServices.BookingSagaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSucceededConsumer {

    private final ObjectMapper objectMapper;
    private final BookingSagaOrchestrator bookingSagaOrchestrator;

    @KafkaListener(
            topics = "payment.events",
            groupId = "booking-payment-result-group"
    )
    public void consume(String message) {

        System.out.println(
                "========== PAYMENT SUCCEEDED RECEIVED =========="
        );

        System.out.println(
                "Payload = " + message
        );

        try {

            PaymentSucceededEvent event =
                    objectMapper.readValue(
                            message,
                            PaymentSucceededEvent.class
                    );

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
                    "Payment ID = "
                            + event.getPaymentId()
            );

            System.out.println(
                    "Amount = "
                            + event.getAmount()
            );

            bookingSagaOrchestrator.handlePaymentSucceeded(event);

            System.out.println(
                    "========== PAYMENT SUCCESS PROCESSED ==========");

            System.out.println(
                    "========== PAYMENT SUCCESS =========="
            );

        } catch (Exception ex) {

            System.err.println(
                    "FAILED TO PROCESS PAYMENT_SUCCEEDED"
            );

            ex.printStackTrace();

            throw new RuntimeException(
                    "Payment success event processing failed",
                    ex
            );
        }
    }
}
