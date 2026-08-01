package com.movieTicket.CategoryService.mapper;

import com.movieTicket.CategoryService.dtos.CreateTheatreRequest;
import com.movieTicket.CategoryService.dtos.TheatreResponse;
import com.movieTicket.CategoryService.dtos.UpdateTheatreRequest;
import com.movieTicket.CategoryService.entity.Theatre;
import com.movieTicket.CategoryService.enums.ScreenStatus;
import org.springframework.stereotype.Component;

@Component
public class TheatreMapper {

    public Theatre toEntity(CreateTheatreRequest request) {

        return Theatre.builder()
                .name(request.getName())
                .city(request.getCity())
                .address(request.getAddress())
                .status(ScreenStatus.ACTIVE)
                .build();
    }

    public TheatreResponse toResponse(Theatre theatre) {

        return TheatreResponse.builder()
                .theatreId(theatre.getTheatreId())
                .name(theatre.getName())
                .city(theatre.getCity())
                .address(theatre.getAddress())
                .status(theatre.getStatus())
                .build();
    }

    public void updateTheatreFromRequest(UpdateTheatreRequest request,
                                         Theatre theatre) {

        theatre.setName(request.getName());
        theatre.setCity(request.getCity());
        theatre.setAddress(request.getAddress());
        theatre.setStatus(request.getStatus());
    }
}
