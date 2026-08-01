package com.movieTicket.CategoryService.dtos;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateScreenRequest {

    @NotNull
    private Long theatreId;

    @NotBlank
    private String name;
}
