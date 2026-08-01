package com.movieTicket.CategoryService.dtos;

import com.movieTicket.CategoryService.enums.ShowStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowResponse {

    private Long showId;

    private Long movieId;

    private Long screenId;

    private LocalDate showDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private ShowStatus status;
}
