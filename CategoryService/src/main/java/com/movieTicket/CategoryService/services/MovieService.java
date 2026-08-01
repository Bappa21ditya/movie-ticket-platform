package com.movieTicket.CategoryService.services;

import com.movieTicket.CategoryService.dtos.CreateMovieRequest;
import com.movieTicket.CategoryService.dtos.MovieResponse;
import com.movieTicket.CategoryService.dtos.UpdateMovieRequest;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface MovieService {

    MovieResponse createMovie(CreateMovieRequest request);

    MovieResponse updateMovie(Long movieId,
                              UpdateMovieRequest request);

    MovieResponse getMovie(Long movieId);

    List<MovieResponse> getAllMovies();

    void deactivateMovie(Long movieId);
}
