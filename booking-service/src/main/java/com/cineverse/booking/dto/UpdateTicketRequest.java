package com.cineverse.booking.dto;
import com.cineverse.booking.enums.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketRequest {
    @NotBlank
    private String qrCode;

    @NotNull
    private TicketStatus status;
}
