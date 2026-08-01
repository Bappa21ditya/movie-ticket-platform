package com.movieTicket.CategoryService.serviceImpl;

import com.movieTicket.CategoryService.dtos.CreateScreenRequest;
import com.movieTicket.CategoryService.dtos.ScreenResponse;
import com.movieTicket.CategoryService.dtos.UpdateScreenRequest;
import com.movieTicket.CategoryService.entity.Screen;
import com.movieTicket.CategoryService.entity.Theatre;
import com.movieTicket.CategoryService.enums.ScreenStatus;
import com.movieTicket.CategoryService.exception.ResourceNotFoundException;
import com.movieTicket.CategoryService.mapper.ScreenMapper;
import com.movieTicket.CategoryService.repositories.ScreenRepository;
import com.movieTicket.CategoryService.repositories.TheatreRepository;
import com.movieTicket.CategoryService.services.ScreenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScreenServiceImpl implements ScreenService {

    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenMapper screenMapper;

    @Override
    public ScreenResponse createScreen(CreateScreenRequest request) {

        Theatre theatre = theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Theatre not found : " + request.getTheatreId()));

        Screen screen = screenMapper.toEntity(request);

        screen.setTheatre(theatre);

        Screen savedScreen = screenRepository.save(screen);

        return screenMapper.toResponse(savedScreen);
    }

    @Override
    public ScreenResponse updateScreen(Long screenId,
                                       UpdateScreenRequest request) {

        Screen screen = getScreenOrThrow(screenId);

        screenMapper.updateScreenFromRequest(request, screen);

        Screen updatedScreen = screenRepository.save(screen);

        return screenMapper.toResponse(updatedScreen);
    }

    @Override
    @Transactional(readOnly = true)
    public ScreenResponse getScreen(Long screenId) {

        Screen screen = getScreenOrThrow(screenId);

        return screenMapper.toResponse(screen);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScreenResponse> getScreensByTheatre(Long theatreId) {

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Theatre not found : " + theatreId));

        return screenRepository.findByTheatre(theatre)
                .stream()
                .map(screenMapper::toResponse)
                .toList();
    }

    @Override
    public void deactivateScreen(Long screenId) {

        Screen screen = getScreenOrThrow(screenId);

        screen.setStatus(ScreenStatus.INACTIVE);
    }

    private Screen getScreenOrThrow(Long screenId) {

        return screenRepository.findById(screenId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Screen not found : " + screenId));
    }
}
