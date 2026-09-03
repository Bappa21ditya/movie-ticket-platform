package com.cineverse.booking.kafka.consumer;


import com.cineverse.booking.kafka.dtos.PaymentRequestedEvent;
import com.cineverse.booking.kafka.dtos.PaymentSucceededEvent;
import com.cineverse.booking.kafka.outbox.OutboxEvent;
import com.cineverse.booking.kafka.outbox.OutboxEventRepository;
import com.cineverse.booking.kafka.outbox.OutboxStatus;
import com.cineverse.booking.payment.dto.CreatePaymentRequest;
import com.cineverse.booking.payment.dto.PaymentResponse;
import com.cineverse.booking.payment.enums.PaymentStatus;
import com.cineverse.booking.payment.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRequestedConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final OutboxEventRepository outboxEventRepository;

    @KafkaListener(
            topics = "booking.payment",
            groupId = "payment-requested-group"
    )
    public void consume(String message) {

        System.out.println(
                " PAYMENT REQUESTED RECEIVED"
        );

        System.out.println(
                "Payload = " + message
        );

        try {

            PaymentRequestedEvent event =
                    objectMapper.readValue(
                            message,
                            PaymentRequestedEvent.class
                    );

            System.out.println(
                    "Saga ID = " + event.getSagaId()
            );

            System.out.println(
                    "Booking ID = " + event.getBookingId()
            );

            System.out.println(
                    "Amount = " + event.getAmount()
            );

            System.out.println(
                    "Payment Method = "
                            + event.getPaymentMethod()
            );

            PaymentResponse paymentResponse =
                    paymentService.createPayment(
                            CreatePaymentRequest.builder()
                                    .bookingId(
                                            event.getBookingId()
                                    )
                                    .amount(
                                            event.getAmount()
                                    )
                                    .paymentMethod(
                                            event.getPaymentMethod()
                                    )
                                    .build()
                    );

            System.out.println(
                    "PAYMENT RESULT = "
                            + paymentResponse.getStatus()
            );

            if (paymentResponse.getStatus() == PaymentStatus.SUCCESS) {

                PaymentSucceededEvent successEvent =
                        PaymentSucceededEvent.builder()
                                .eventId(UUID.randomUUID())
                                .sagaId(event.getSagaId())
                                .bookingId(event.getBookingId())
                                .userId(event.getUserId())
                                .paymentId(paymentResponse.getPaymentId())
                                .amount(paymentResponse.getAmount())
                                .occurredAt(OffsetDateTime.now())
                                .build();

                JsonNode payload =
                        objectMapper.valueToTree(successEvent);

                OutboxEvent outboxEvent =
                        OutboxEvent.builder()
                                .eventId(successEvent.getEventId())
                                .aggregateId(paymentResponse.getBookingId())
                                .aggregateType("PAYMENT")
                                .eventType("PAYMENT_SUCCEEDED")
                                .payload(payload)
                                .status(OutboxStatus.PENDING)
                                .retryCount(0)
                                .createdAt(OffsetDateTime.now())
                                .build();

                outboxEventRepository.saveAndFlush(outboxEvent);

                System.out.println(
                        "PAYMENT_SUCCEEDED OUTBOX SAVED = "
                                + successEvent.getEventId()
                );
            }

            // next: publish PAYMENT_SUCCEEDED
            // or PAYMENT_FAILED

        } catch (Exception ex) {

            System.err.println(
                    "FAILED TO PROCESS PAYMENT_REQUESTED"
            );

            ex.printStackTrace();

            throw new RuntimeException(
                    "Payment event processing failed",
                    ex
            );
        }
    }
}
