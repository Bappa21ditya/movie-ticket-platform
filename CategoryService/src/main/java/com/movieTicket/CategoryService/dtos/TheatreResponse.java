package com.movieTicket.CategoryService.dtos;
import jakarta.persistence.*;
import lombok.*;

import com.movieTicket.CategoryService.enums.ScreenStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheatreResponse {

    private Long theatreId;

    private String name;

    private String city;

    private String address;

    private ScreenStatus status;
}
