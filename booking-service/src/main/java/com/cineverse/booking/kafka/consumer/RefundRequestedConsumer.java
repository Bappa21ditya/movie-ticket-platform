package com.cineverse.booking.kafka.consumer;

import com.cineverse.booking.kafka.dtos.PaymentSucceededEvent;
import com.cineverse.booking.kafka.dtos.RefundRequestedEvent;
import com.cineverse.booking.payment.service.PaymentService;
import com.cineverse.booking.sagaServices.BookingSagaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class RefundRequestedConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final BookingSagaOrchestrator bookingSagaOrchestrator;

    @KafkaListener(
            topics = "booking.refund-requested",
            groupId = "payment-service"
    )
    public void consume(String message) {

        try {

            System.out.println(
                    "========== REFUND REQUESTED EVENT RECEIVED =========="
            );

            RefundRequestedEvent event =
                    objectMapper.readValue(
                            message,
                            RefundRequestedEvent.class
                    );

            System.out.println(
                    "Saga ID = " + event.getSagaId()
            );

            System.out.println(
                    "Booking ID = " + event.getBookingId()
            );

            bookingSagaOrchestrator.processRefund(event);

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to process REFUND_REQUESTED",
                    e
            );
        }
    }
}
