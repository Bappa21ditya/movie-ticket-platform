package com.cineverse.booking.saga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface SagaInstanceRepository  extends JpaRepository<SagaInstance, UUID> {

    Optional<SagaInstance> findByBookingId(UUID bookingId);

    List<SagaInstance> findByStatus(SagaStatus status);

    List<SagaInstance> findByCurrentStepAndStatus(
            SagaStep currentStep,
            SagaStatus status
    );
}
