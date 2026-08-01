package com.movieTicket.CategoryService.mapper;

import com.movieTicket.CategoryService.dtos.CreateMovieRequest;
import com.movieTicket.CategoryService.dtos.MovieResponse;
import com.movieTicket.CategoryService.dtos.UpdateMovieRequest;
import com.movieTicket.CategoryService.entity.Movie;
import com.movieTicket.CategoryService.enums.MovieStatus;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    public Movie toEntity(CreateMovieRequest request) {

        return Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .duration(request.getDuration())
                .language(request.getLanguage())
                .genre(request.getGenre())
                .releaseDate(request.getReleaseDate())
                .status(MovieStatus.COMING_SOON)
                .build();
    }

    public MovieResponse toResponse(Movie movie) {

        return MovieResponse.builder()
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .duration(movie.getDuration())
                .language(movie.getLanguage())
                .genre(movie.getGenre())
                .releaseDate(movie.getReleaseDate())
                .status(movie.getStatus())
                .build();
    }

    public void updateMovieFromRequest(UpdateMovieRequest request,
                                       Movie movie) {

        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration());
        movie.setLanguage(request.getLanguage());
        movie.setGenre(request.getGenre());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setStatus(request.getStatus());
    }
}
