package com.movieTicket.CategoryService.serviceImpl;


import com.movieTicket.CategoryService.dtos.CreateMovieRequest;
import com.movieTicket.CategoryService.dtos.MovieResponse;
import com.movieTicket.CategoryService.dtos.UpdateMovieRequest;
import com.movieTicket.CategoryService.entity.Movie;
import com.movieTicket.CategoryService.enums.MovieStatus;
import com.movieTicket.CategoryService.exception.ResourceAlreadyExistsException;
import com.movieTicket.CategoryService.exception.ResourceNotFoundException;
import com.movieTicket.CategoryService.mapper.MovieMapper;
import com.movieTicket.CategoryService.repositories.MovieRepository;
import com.movieTicket.CategoryService.services.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    private final MovieMapper movieMapper;

    @Override
    public MovieResponse createMovie(CreateMovieRequest request) {

        if (movieRepository.existsByTitle(request.getTitle())) {
            throw new ResourceAlreadyExistsException(
                    "Movie already exists with title : " + request.getTitle());
        }

        Movie movie = movieMapper.toEntity(request);

        Movie savedMovie = movieRepository.save(movie);

        return movieMapper.toResponse(savedMovie);

    }

    @Override
    public MovieResponse updateMovie(Long movieId,
                                     UpdateMovieRequest request) {

            Movie movie = getMovieOrThrow(movieId);

            movieMapper.updateMovieFromRequest(request, movie);

            Movie updatedMovie = movieRepository.save(movie);

            return movieMapper.toResponse(updatedMovie);
        }


    @Override
    @Transactional(readOnly = true)
    public MovieResponse getMovie(Long movieId) {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie not found : " + movieId));

        return movieMapper.toResponse(movie);

    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getAllMovies() {

        return movieRepository.findAll()
                .stream()
                .map(movieMapper::toResponse)
                .toList();

    }

    @Override
    public void deactivateMovie(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie not found : " + movieId));

        movie.setStatus(MovieStatus.INACTIVE);

    }
private Movie getMovieOrThrow(Long movieId) {
    return movieRepository.findById(movieId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Movie not found : " + movieId));
  }

}
