package com.cineverse.booking.sagaServices;

import com.cineverse.booking.saga.SagaInstance;
import com.cineverse.booking.saga.SagaInstanceRepository;
import com.cineverse.booking.saga.SagaStatus;
import com.cineverse.booking.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SagaStateService {


    private final SagaInstanceRepository sagaInstanceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompensationFailed(
            UUID sagaId,
            Exception ex) {

        System.out.println("MARK COMPENSATION FAILED");
        System.out.println("Saga ID received = " + sagaId);


        SagaInstance saga = sagaInstanceRepository
                .findById(sagaId).
        orElseThrow(() ->
                new IllegalStateException(
                        "Saga not found: " + sagaId
                )
        );
        System.out.println(
                "Saga found. Booking ID = " + saga.getBookingId()
        );

        saga.setCurrentStep(SagaStep.COMPENSATING);
        saga.setStatus(SagaStatus.IN_PROGRESS);
        saga.setRetryCount(saga.getRetryCount() + 1);
        saga.setLastError(ex.getMessage());
        saga.setUpdatedAt(OffsetDateTime.now());

        sagaInstanceRepository.save(saga);
    }
}
