package com.cineverse.booking.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {
    @NotBlank
    private String ticketNumber;

    @NotBlank
    private String qrCode;
}
