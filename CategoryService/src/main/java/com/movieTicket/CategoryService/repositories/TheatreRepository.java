package com.movieTicket.CategoryService.repositories;

import com.movieTicket.CategoryService.entity.Theatre;
import com.movieTicket.CategoryService.enums.ScreenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository  extends JpaRepository<Theatre, Long> {

    List<Theatre> findByCity(String city);

    List<Theatre> findByStatus(ScreenStatus status);

    boolean existsByName(String name);

}
