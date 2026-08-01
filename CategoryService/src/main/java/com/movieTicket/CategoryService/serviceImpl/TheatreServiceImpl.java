package com.movieTicket.CategoryService.serviceImpl;

import com.movieTicket.CategoryService.dtos.CreateTheatreRequest;
import com.movieTicket.CategoryService.dtos.TheatreResponse;
import com.movieTicket.CategoryService.dtos.UpdateTheatreRequest;
import com.movieTicket.CategoryService.entity.Theatre;
import com.movieTicket.CategoryService.enums.ScreenStatus;
import com.movieTicket.CategoryService.exception.ResourceAlreadyExistsException;
import com.movieTicket.CategoryService.exception.ResourceNotFoundException;
import com.movieTicket.CategoryService.mapper.TheatreMapper;
import com.movieTicket.CategoryService.repositories.TheatreRepository;
import com.movieTicket.CategoryService.services.TheatreService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TheatreServiceImpl implements TheatreService {

    private final TheatreRepository theatreRepository;
    private final TheatreMapper theatreMapper;

    @Override
    public TheatreResponse createTheatre(CreateTheatreRequest request) {

        if (theatreRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException(
                    "Theatre already exists with name : " + request.getName());
        }

        Theatre theatre = theatreMapper.toEntity(request);

        Theatre savedTheatre = theatreRepository.save(theatre);

        return theatreMapper.toResponse(savedTheatre);
    }

    @Override
    public TheatreResponse updateTheatre(Long theatreId,
                                         UpdateTheatreRequest request) {

        Theatre theatre = getTheatreOrThrow(theatreId);

        theatreMapper.updateTheatreFromRequest(request, theatre);

        Theatre updatedTheatre = theatreRepository.save(theatre);

        return theatreMapper.toResponse(updatedTheatre);
    }

    @Override
    @Transactional(readOnly = true)
    public TheatreResponse getTheatre(Long theatreId) {

        Theatre theatre = getTheatreOrThrow(theatreId);

        return theatreMapper.toResponse(theatre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TheatreResponse> getAllTheatres() {

        return theatreRepository.findAll()
                .stream()
                .map(theatreMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TheatreResponse> getTheatresByCity(String city) {

        return theatreRepository.findByCity(city)
                .stream()
                .map(theatreMapper::toResponse)
                .toList();
    }

    @Override
    public void deactivateTheatre(Long theatreId) {

        Theatre theatre = getTheatreOrThrow(theatreId);

        theatre.setStatus(ScreenStatus.INACTIVE);
    }

    private Theatre getTheatreOrThrow(Long theatreId) {

        return theatreRepository.findById(theatreId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Theatre not found : " + theatreId));
    }
}
