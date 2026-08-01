package com.movieTicket.CategoryService.mapper;

import com.movieTicket.CategoryService.dtos.CreateScreenRequest;
import com.movieTicket.CategoryService.dtos.ScreenResponse;
import com.movieTicket.CategoryService.dtos.UpdateScreenRequest;
import com.movieTicket.CategoryService.entity.Screen;
import com.movieTicket.CategoryService.enums.ScreenStatus;
import org.springframework.stereotype.Component;

@Component
public class ScreenMapper {
    public Screen toEntity(CreateScreenRequest request) {

        return Screen.builder()
                .name(request.getName())
                .status(ScreenStatus.ACTIVE)
                .build();
    }

    public ScreenResponse toResponse(Screen screen) {

        return ScreenResponse.builder()
                .screenId(screen.getScreenId())
                .theatreId(screen.getTheatre().getTheatreId())
                .name(screen.getName())
                .status(screen.getStatus())
                .build();
    }

    public void updateScreenFromRequest(UpdateScreenRequest request,
                                        Screen screen) {

        screen.setName(request.getName());
        screen.setStatus(request.getStatus());
    }
}
