package com.movieTicket.CategoryService.dtos;
import com.movieTicket.CategoryService.enums.Genre;
import com.movieTicket.CategoryService.enums.Language;
import com.movieTicket.CategoryService.enums.MovieStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMovieRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Integer duration;

    @NotNull
    private Language language;

    @NotNull
    private Genre genre;

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    private MovieStatus status;
}
