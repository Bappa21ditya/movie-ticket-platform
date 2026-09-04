package com.cineverse.booking.kafka.outbox;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

   // private static final String TOPIC =
   //         "booking.seat-hold";

    private final OutboxEventRepository outboxEventRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;


    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findTop100ByStatusOrderByCreatedAtAsc(
                                OutboxStatus.PENDING
                        );

        for (OutboxEvent event : events) {

            try {


                String TOPIC =
                        getTopic(event.getEventType());

                System.out.println(
                        "Publishing Outbox Event = "
                                + event.getEventId()
                );

                kafkaTemplate
                        .send(
                                TOPIC,
                                event.getAggregateId().toString(),
                                event.getPayload().toString()
                        )
                        .get();


                // Kafka accepted the message
                event.setStatus(
                        OutboxStatus.PUBLISHED
                );

                event.setPublishedAt(
                        OffsetDateTime.now()
                );

                outboxEventRepository.save(event);


                System.out.println(
                        "========== OUTBOX PUBLISHED =========="
                );

                System.out.println(
                        "Event ID = "
                                + event.getEventId()
                );

                System.out.println(
                        "Event Type = "
                                + event.getEventType()
                );

                System.out.println(
                        "Topic = "
                                + TOPIC
                );

                System.out.println(
                        "======================================"
                );


            } catch (Exception ex) {

                event.setRetryCount(
                        event.getRetryCount() + 1
                );

                outboxEventRepository.save(event);

                System.err.println(
                        "========== OUTBOX PUBLISH FAILED =========="
                );

                System.err.println(
                        "Event ID = "
                                + event.getEventId()
                );

                ex.printStackTrace();
            }
        }
    }
    private String getTopic(String eventType) {

        return switch (eventType) {

            case "SEAT_HOLD_REQUESTED",
                 "SEAT_HELD" ->
                    "booking.seat-hold";

            case "PAYMENT_REQUESTED" ->
                    "booking.payment";

            case "PAYMENT_SUCCEEDED" ->
                    "payment.events";

            case "SEAT_CONFIRM_REQUESTED" ->
                    "booking.seat-confirm";

            case "RELEASE_SEATS_REQUESTED" ->
                    "release-seats-requested";

            case "REFUND_REQUESTED" ->
                    "booking.refund-requested";


            default ->
                    throw new IllegalArgumentException(
                            "Unknown event type: " + eventType
                    );
        };
    }
}
