package com.movieTicket.CategoryService.services;


import com.movieTicket.CategoryService.dtos.CreateShowRequest;
import com.movieTicket.CategoryService.dtos.ShowResponse;
import com.movieTicket.CategoryService.dtos.UpdateShowRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface ShowService {

    ShowResponse createShow(CreateShowRequest request);

    ShowResponse updateShow(Long showId,
                            UpdateShowRequest request);

    ShowResponse getShow(Long showId);

    List<ShowResponse> getShowsByMovie(Long movieId);

    List<ShowResponse> getShowsByScreen(Long screenId);

    List<ShowResponse> getShowsByDate(LocalDate date);

    void cancelShow(Long showId);
}
