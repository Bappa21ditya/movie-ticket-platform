package com.movieTicket.CategoryService.movieController;

import com.movieTicket.CategoryService.dtos.CreateTheatreRequest;
import com.movieTicket.CategoryService.dtos.TheatreResponse;
import com.movieTicket.CategoryService.dtos.UpdateTheatreRequest;
import com.movieTicket.CategoryService.services.TheatreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/theatres")
@RequiredArgsConstructor
public class TheatreController {
    private final TheatreService theatreService;

    @PostMapping
    public ResponseEntity<TheatreResponse> createTheatre(
            @Valid @RequestBody CreateTheatreRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(theatreService.createTheatre(request));
    }

    @PutMapping("/{theatreId}")
    public ResponseEntity<TheatreResponse> updateTheatre(
            @PathVariable Long theatreId,
            @Valid @RequestBody UpdateTheatreRequest request) {

        return ResponseEntity.ok(
                theatreService.updateTheatre(theatreId, request));
    }

    @GetMapping("/{theatreId}")
    public ResponseEntity<TheatreResponse> getTheatre(
            @PathVariable Long theatreId) {

        return ResponseEntity.ok(
                theatreService.getTheatre(theatreId));
    }

    @GetMapping
    public ResponseEntity<List<TheatreResponse>> getAllTheatres() {

        return ResponseEntity.ok(
                theatreService.getAllTheatres());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<TheatreResponse>> getTheatresByCity(
            @PathVariable String city) {

        return ResponseEntity.ok(
                theatreService.getTheatresByCity(city));
    }

    @DeleteMapping("/{theatreId}")
    public ResponseEntity<Void> deactivateTheatre(
            @PathVariable Long theatreId) {

        theatreService.deactivateTheatre(theatreId);

        return ResponseEntity.noContent().build();
    }
}
