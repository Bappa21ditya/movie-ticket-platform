package com.cineverse.booking.kafka.consumer;


import com.cineverse.booking.kafka.dtos.PaymentRequestedEvent;
import com.cineverse.booking.payment.dto.CreatePaymentRequest;
import com.cineverse.booking.payment.dto.PaymentResponse;
import com.cineverse.booking.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentRequestedConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

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

            PaymentResponse response =
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
                            + response.getStatus()
            );

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
