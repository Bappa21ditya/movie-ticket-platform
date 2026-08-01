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
public class UpdateScreenRequest {

    @NotBlank
    private String name;

    @NotNull
    private ScreenStatus status;
}
