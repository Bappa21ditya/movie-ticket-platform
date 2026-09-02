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

   // @Scheduled(fixedDelay = 30000)
    public void recoverSagas() {

        recoverCompensatingSagas();

        recoverRefundPendingSagas();
    }

    private void recoverCompensatingSagas() {

        List<SagaInstance> sagas =
                sagaInstanceRepository.findByCurrentStepAndStatus(
                        SagaStep.COMPENSATING,
                        SagaStatus.IN_PROGRESS
                );

        for (SagaInstance saga : sagas) {

            if (saga.getRetryCount() >= MAX_RETRIES) {

                saga.setStatus(SagaStatus.FAILED);

                saga.setLastError(
                        "Maximum compensation retries exhausted. "
                                + "Manual intervention required."
                );

                saga.setUpdatedAt(
                        OffsetDateTime.now()
                );

                sagaInstanceRepository.save(saga);

                continue;
            }

            try {
                System.out.println("=================================");
                System.out.println("SCHEDULER FOUND REFUND_PENDING SAGA");
                System.out.println("Saga ID: " + saga.getSagaId());
                System.out.println("Booking ID: " + saga.getBookingId());
                System.out.println("=================================");

                bookingSagaOrchestrator.retryCompensation(
                        saga.getSagaId()
                );

            } catch (Exception ex) {

                System.out.println(
                        "Automatic compensation recovery failed: "
                                + saga.getSagaId()
                );
            }
        }
    }

    private void recoverRefundPendingSagas() {

        List<SagaInstance> sagas =
                sagaInstanceRepository.findByCurrentStepAndStatus(
                        SagaStep.REFUND_PENDING,
                        SagaStatus.IN_PROGRESS
                );

        for (SagaInstance saga : sagas) {

            if (saga.getRetryCount() >= MAX_RETRIES) {

                saga.setStatus(SagaStatus.FAILED);

                saga.setLastError(
                        "Maximum refund retries exhausted. "
                                + "Manual intervention required."
                );

                saga.setUpdatedAt(
                        OffsetDateTime.now()
                );

                sagaInstanceRepository.save(saga);

                continue;
            }

            try {

                bookingSagaOrchestrator.retryCompensation(
                        saga.getSagaId()
                );

            } catch (Exception ex) {

                System.out.println(
                        "Automatic refund recovery failed: "
                                + saga.getSagaId()
                );
            }
        }
    }
}