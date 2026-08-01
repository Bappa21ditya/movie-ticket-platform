package com.movieTicket.CategoryService.services;


import com.movieTicket.CategoryService.dtos.CreateTheatreRequest;
import com.movieTicket.CategoryService.dtos.TheatreResponse;
import com.movieTicket.CategoryService.dtos.UpdateTheatreRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TheatreService {

    TheatreResponse createTheatre(CreateTheatreRequest request);

    TheatreResponse updateTheatre(Long theatreId,
                                  UpdateTheatreRequest request);

    TheatreResponse getTheatre(Long theatreId);

    List<TheatreResponse> getAllTheatres();

    List<TheatreResponse> getTheatresByCity(String city);

    void deactivateTheatre(Long theatreId);

}
