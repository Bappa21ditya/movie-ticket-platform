package com.cineverse.booking.restClient;

import com.cineverse.booking.dto.sagaClient.ConfirmSeatRequest;
import com.cineverse.booking.dto.sagaClient.CreateSeatHoldRequest;
import com.cineverse.booking.dto.sagaClient.ReleaseSeatRequest;
import com.cineverse.booking.dto.sagaClient.SeatHoldResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final RestClient restClient;

    public SeatHoldResponse createSeatHold(
            CreateSeatHoldRequest request) {

        return restClient
                .post()
                .uri("/api/v1/seat-holds")
                .body(request)
                .retrieve()
                .body(SeatHoldResponse.class);
    }
    public void confirmSeat(Long showSeatId, UUID bookingId) {

        restClient
                .post()
                .uri("/api/v1/seat-holds/confirm")
                .body(new ConfirmSeatRequest(showSeatId, bookingId))
                .retrieve()
                .toBodilessEntity();
    }
    public void releaseSeat(Long showSeatId, UUID bookingId) {

        restClient
                .post()
                .uri("/api/v1/seat-holds/release")
                .body(new ReleaseSeatRequest(showSeatId, bookingId))
                .retrieve()
                .toBodilessEntity();
    }
}
