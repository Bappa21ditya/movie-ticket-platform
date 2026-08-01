package com.movieTicket.CategoryService.serviceImpl;

import com.movieTicket.CategoryService.dtos.CreateShowRequest;
import com.movieTicket.CategoryService.dtos.ShowResponse;
import com.movieTicket.CategoryService.dtos.UpdateShowRequest;
import com.movieTicket.CategoryService.entity.Movie;
import com.movieTicket.CategoryService.entity.Screen;
import com.movieTicket.CategoryService.entity.Show;
import com.movieTicket.CategoryService.enums.ShowStatus;
import com.movieTicket.CategoryService.exception.ResourceNotFoundException;
import com.movieTicket.CategoryService.repositories.MovieRepository;
import com.movieTicket.CategoryService.repositories.ScreenRepository;
import com.movieTicket.CategoryService.repositories.ShowRepository;
import com.movieTicket.CategoryService.services.ShowService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.movieTicket.CategoryService.mapper.ShowMapper;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;

    private final MovieRepository movieRepository;

    private final ScreenRepository screenRepository;

    private final ShowMapper showMapper;

    @Override
    public ShowResponse createShow(CreateShowRequest request) {

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie not found : " + request.getMovieId()));

        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Screen not found : " + request.getScreenId()));

        Show show = showMapper.toEntity(request);

        show.setMovie(movie);
        show.setScreen(screen);

        Show savedShow = showRepository.save(show);

        return showMapper.toResponse(savedShow);
    }

    @Override
    public ShowResponse updateShow(Long showId,
                                   UpdateShowRequest request) {

        Show show = getShowOrThrow(showId);

        showMapper.updateShowFromRequest(request, show);

        Show updatedShow = showRepository.save(show);

        return showMapper.toResponse(updatedShow);
    }

    @Override
    @Transactional(readOnly = true)
    public ShowResponse getShow(Long showId) {

        return showMapper.toResponse(getShowOrThrow(showId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowResponse> getShowsByMovie(Long movieId) {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie not found : " + movieId));

        return showRepository.findByMovie(movie)
                .stream()
                .map(showMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowResponse> getShowsByScreen(Long screenId) {

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Screen not found : " + screenId));

        return showRepository.findByScreen(screen)
                .stream()
                .map(showMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowResponse> getShowsByDate(LocalDate date) {

        return showRepository.findByShowDate(date)
                .stream()
                .map(showMapper::toResponse)
                .toList();
    }

    @Override
    public void cancelShow(Long showId) {

        Show show = getShowOrThrow(showId);

        show.setStatus(ShowStatus.CANCELLED);
    }

    private Show getShowOrThrow(Long showId) {

        return showRepository.findById(showId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Show not found : " + showId));
    }
}
