package com.cineverse.booking.restClient;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    public RestClient inventoryRestClient(
            @Value("${inventory.service.url}") String inventoryServiceUrl) {

        return RestClient.builder()
                .baseUrl(inventoryServiceUrl)
                .build();
    }
}
