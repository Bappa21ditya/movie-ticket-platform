package com.movieTicket.CategoryService.dtos;
import com.movieTicket.CategoryService.enums.ScreenStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTheatreRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String city;

    @NotBlank
    private String address;

    @NotNull
    private ScreenStatus status;
}
