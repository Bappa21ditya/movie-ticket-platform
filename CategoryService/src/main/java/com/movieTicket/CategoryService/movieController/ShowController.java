package com.movieTicket.CategoryService.movieController;

import com.movieTicket.CategoryService.dtos.CreateShowRequest;
import com.movieTicket.CategoryService.dtos.ShowResponse;
import com.movieTicket.CategoryService.dtos.UpdateShowRequest;
import com.movieTicket.CategoryService.services.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
public class ShowController {
    private final ShowService showService;

    @PostMapping
    public ResponseEntity<ShowResponse> createShow(
            @Valid @RequestBody CreateShowRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(showService.createShow(request));
    }

    @PutMapping("/{showId}")
    public ResponseEntity<ShowResponse> updateShow(
            @PathVariable Long showId,
            @Valid @RequestBody UpdateShowRequest request) {

        return ResponseEntity.ok(
                showService.updateShow(showId, request));
    }

    @GetMapping("/{showId}")
    public ResponseEntity<ShowResponse> getShow(
            @PathVariable Long showId) {

        return ResponseEntity.ok(
                showService.getShow(showId));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowResponse>> getShowsByMovie(
            @PathVariable Long movieId) {

        return ResponseEntity.ok(
                showService.getShowsByMovie(movieId));
    }

    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<ShowResponse>> getShowsByScreen(
            @PathVariable Long screenId) {

        return ResponseEntity.ok(
                showService.getShowsByScreen(screenId));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<ShowResponse>> getShowsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return ResponseEntity.ok(
                showService.getShowsByDate(date));
    }

    @DeleteMapping("/{showId}")
    public ResponseEntity<Void> cancelShow(
            @PathVariable Long showId) {

        showService.cancelShow(showId);

        return ResponseEntity.noContent().build();
    }

}
