package com.movieTicket.CategoryService.dtos;
import com.movieTicket.CategoryService.enums.Genre;
import com.movieTicket.CategoryService.enums.Language;
import com.movieTicket.CategoryService.enums.MovieStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponse {

    private Long movieId;

    private String title;

    private String description;

    private Integer duration;

    private Language language;

    private Genre genre;

    private LocalDate releaseDate;

    private MovieStatus status;
}
