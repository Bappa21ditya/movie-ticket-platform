package com.movieTicket.CategoryService.mapper;

import com.movieTicket.CategoryService.dtos.CreateShowRequest;
import com.movieTicket.CategoryService.dtos.ShowResponse;
import com.movieTicket.CategoryService.dtos.UpdateShowRequest;
import com.movieTicket.CategoryService.entity.Show;
import com.movieTicket.CategoryService.enums.ShowStatus;
import org.springframework.stereotype.Component;

@Component
public class ShowMapper {
    public Show toEntity(CreateShowRequest request) {

        return Show.builder()
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ShowStatus.SCHEDULED)
                .build();
    }

    public ShowResponse toResponse(Show show) {

        return ShowResponse.builder()
                .showId(show.getShowId())
                .movieId(show.getMovie().getMovieId())
                .screenId(show.getScreen().getScreenId())
                .showDate(show.getShowDate())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .status(show.getStatus())
                .build();
    }

    public void updateShowFromRequest(UpdateShowRequest request,
                                      Show show) {

        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getEndTime());
        show.setStatus(request.getStatus());
    }
}
