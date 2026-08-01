package com.movieTicket.CategoryService.repositories;

import com.movieTicket.CategoryService.entity.Movie;
import com.movieTicket.CategoryService.entity.Screen;
import com.movieTicket.CategoryService.entity.Show;
import com.movieTicket.CategoryService.enums.ShowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByMovie_MovieId(Long movieId);

    List<Show> findByScreen_ScreenId(Long screenId);

    List<Show> findByShowDate(LocalDate showDate);

    List<Show> findByStatus(ShowStatus status);

    List<Show> findByMovie_MovieIdAndShowDate(
            Long movieId,
            LocalDate showDate
    );

    List<Show> findByMovie(Movie movie);

    List<Show> findByScreen(Screen screen);

}
