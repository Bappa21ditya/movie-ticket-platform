package com.movieTicket.CategoryService.dtos;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTheatreRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String city;

    @NotBlank
    private String address;
}
