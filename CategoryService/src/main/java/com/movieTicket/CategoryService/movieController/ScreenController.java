package com.movieTicket.CategoryService.movieController;

import com.movieTicket.CategoryService.dtos.CreateScreenRequest;
import com.movieTicket.CategoryService.dtos.ScreenResponse;
import com.movieTicket.CategoryService.dtos.UpdateScreenRequest;
import com.movieTicket.CategoryService.services.ScreenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/screens")
@RequiredArgsConstructor
public class ScreenController {
    private final ScreenService screenService;

    @PostMapping
    public ResponseEntity<ScreenResponse> createScreen(
            @Valid @RequestBody CreateScreenRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(screenService.createScreen(request));
    }

    @PutMapping("/{screenId}")
    public ResponseEntity<ScreenResponse> updateScreen(
            @PathVariable Long screenId,
            @Valid @RequestBody UpdateScreenRequest request) {

        return ResponseEntity.ok(
                screenService.updateScreen(screenId, request));
    }

    @GetMapping("/{screenId}")
    public ResponseEntity<ScreenResponse> getScreen(
            @PathVariable Long screenId) {

        return ResponseEntity.ok(
                screenService.getScreen(screenId));
    }

    @GetMapping("/theatre/{theatreId}")
    public ResponseEntity<List<ScreenResponse>> getScreensByTheatre(
            @PathVariable Long theatreId) {

        return ResponseEntity.ok(
                screenService.getScreensByTheatre(theatreId));
    }

    @DeleteMapping("/{screenId}")
    public ResponseEntity<Void> deactivateScreen(
            @PathVariable Long screenId) {

        screenService.deactivateScreen(screenId);

        return ResponseEntity.noContent().build();
    }

}
