package com.movieTicket.CategoryService.services;

import com.movieTicket.CategoryService.dtos.CreateScreenRequest;
import com.movieTicket.CategoryService.dtos.ScreenResponse;
import com.movieTicket.CategoryService.dtos.UpdateScreenRequest;
import com.movieTicket.CategoryService.repositories.ScreenRepository;
import com.movieTicket.CategoryService.repositories.TheatreRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ScreenService {

    ScreenResponse createScreen(CreateScreenRequest request);

    ScreenResponse updateScreen(Long screenId,
                                UpdateScreenRequest request);

    ScreenResponse getScreen(Long screenId);

    List<ScreenResponse> getScreensByTheatre(Long theatreId);

    void deactivateScreen(Long screenId);

}
