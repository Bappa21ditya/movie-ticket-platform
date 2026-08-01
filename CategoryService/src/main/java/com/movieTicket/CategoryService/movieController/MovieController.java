package com.movieTicket.CategoryService.movieController;

import com.movieTicket.CategoryService.dtos.CreateMovieRequest;
import com.movieTicket.CategoryService.dtos.MovieResponse;
import com.movieTicket.CategoryService.dtos.UpdateMovieRequest;
import com.movieTicket.CategoryService.services.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(
            @Valid @RequestBody CreateMovieRequest request) {

        MovieResponse response = movieService.createMovie(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{movieId}")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long movieId,
            @Valid @RequestBody UpdateMovieRequest request) {

        return ResponseEntity.ok(
                movieService.updateMovie(movieId, request));
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieResponse> getMovie(
            @PathVariable Long movieId) {

        return ResponseEntity.ok(
                movieService.getMovie(movieId));
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {

        return ResponseEntity.ok(
                movieService.getAllMovies());
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> deactivateMovie(
            @PathVariable Long movieId) {

        movieService.deactivateMovie(movieId);

        return ResponseEntity.noContent().build();
    }

}
