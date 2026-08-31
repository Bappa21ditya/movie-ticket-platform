package com.cineverse.booking.sagaServices;

import com.cineverse.booking.saga.SagaInstance;
import com.cineverse.booking.saga.SagaInstanceRepository;
import com.cineverse.booking.saga.SagaStatus;
import com.cineverse.booking.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SagaRecoveryService {

    private static final int MAX_RETRIES = 5;

    private final SagaInstanceRepository sagaInstanceRepository;
    private final BookingSagaOrchestrator bookingSagaOrchestrator;

    @Scheduled(fixedDelay = 10000)
    public void recoverCompensatingSagas() {

        List<SagaInstance> sagas =
                sagaInstanceRepository.findByCurrentStepAndStatus(
                        SagaStep.COMPENSATING,
                        SagaStatus.IN_PROGRESS
                );

        for (SagaInstance saga : sagas) {


            if (saga.getRetryCount() >= MAX_RETRIES) {


                // Stop retrying after maximum attempts
                saga.setStatus(SagaStatus.FAILED);

                saga.setLastError(
                        "Maximum compensation retries exhausted. "
                                + "Manual intervention required."
                );

                saga.setUpdatedAt(
                        OffsetDateTime.now()
                );

                sagaInstanceRepository.save(saga);

                System.out.println(
                        "Saga permanently failed after max retries: "
                                + saga.getSagaId()
                );

                continue;
            }

            try {

//                bookingSagaOrchestrator.retryCompensation(
//                        saga.getSagaId()
//                );
                bookingSagaOrchestrator.retryCompensation(
                        saga.getSagaId()
                );

            } catch (Exception ex) {

                // retryCompensation already records
                // retryCount + lastError

//                System.out.println(
//                        "Saga recovery failed: "
//                                + saga.getSagaId()
//                );
                System.out.println(
                        "Saga recovery failed: "
                                + saga.getSagaId()
                );
            }
        }
    }
}

