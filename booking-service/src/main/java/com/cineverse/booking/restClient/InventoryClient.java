package com.cineverse.booking.restClient;

import com.cineverse.booking.dto.sagaClient.CreateSeatHoldRequest;
import com.cineverse.booking.dto.sagaClient.SeatHoldResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
}
