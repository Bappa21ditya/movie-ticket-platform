package com.movieTicket.CategoryService.dtos;
import com.movieTicket.CategoryService.enums.ShowStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShowRequest {

    @NotNull
    private LocalDate showDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private ShowStatus status;
}
