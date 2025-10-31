package com.martinia.indigo.author.infrastructure.api.controllers;

import com.martinia.indigo.author.application.CountAllAuthorsUseCaseImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/author")
@Tag(name = "Authors", description = "API for author management")
public class CountAllAuthorsController {

    private final CountAllAuthorsUseCaseImpl countAllAuthorsUseCase;

    @Operation(summary = "Count all authors",
            description = "Retrieves the total number of authors in the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the count of authors",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
            })
    @GetMapping("/count")
    public ResponseEntity<Long> countAllAuthors() {
        return ResponseEntity.ok(this.countAllAuthorsUseCase.countAllAuthors());
    }
}