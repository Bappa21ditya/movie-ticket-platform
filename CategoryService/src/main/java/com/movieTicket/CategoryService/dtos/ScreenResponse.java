package com.movieTicket.CategoryService.dtos;
import jakarta.persistence.*;
import lombok.*;
import com.movieTicket.CategoryService.enums.ScreenStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenResponse {

    private Long screenId;

    private Long theatreId;

    private String name;

    private ScreenStatus status;
}
