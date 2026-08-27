package com.cineverse.booking.saga;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "saga_instance",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_saga_booking", columnNames = "booking_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID sagaId;

    @Column(name = "booking_id", nullable = false, unique = true)
    private UUID bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "saga_type", nullable = false)
    private SagaType sagaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false)
    private SagaStep currentStep;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

}
