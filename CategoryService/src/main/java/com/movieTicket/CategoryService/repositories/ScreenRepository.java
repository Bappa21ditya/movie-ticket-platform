package com.movieTicket.CategoryService.repositories;

import com.movieTicket.CategoryService.entity.Screen;
import com.movieTicket.CategoryService.entity.Theatre;
import com.movieTicket.CategoryService.enums.ScreenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {

    List<Screen> findByTheatre_TheatreId(Long theatreId);

    List<Screen> findByStatus(ScreenStatus status);

    List<Screen> findByTheatre(Theatre theatre);

}
