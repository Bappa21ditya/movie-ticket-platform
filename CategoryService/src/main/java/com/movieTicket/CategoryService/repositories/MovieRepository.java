package com.movieTicket.CategoryService.repositories;

import com.movieTicket.CategoryService.entity.Movie;
import com.movieTicket.CategoryService.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(MovieStatus status);

    Optional<Movie> findByTitle(String title);

    boolean existsByTitle(String title);
}
