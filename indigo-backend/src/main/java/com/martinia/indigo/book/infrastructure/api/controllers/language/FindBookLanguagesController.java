package com.martinia.indigo.book.infrastructure.api.controllers.language;

import com.martinia.indigo.book.domain.ports.usecases.language.FindBookLanguagesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API for book management")
public class FindBookLanguagesController {

	@Resource
	private FindBookLanguagesUseCase useCase;

	@Operation(summary = "Get all book languages",
			description = "Retrieves a list of all unique languages found in the books.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the list of languages",
							content = @Content(mediaType = "application/json",
									schema = @Schema(type = "array", implementation = String.class)))
			})
	@GetMapping(value = "/languages", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> getBookLanguages() {

		List<String> languages = useCase.getBookLanguages();
		return new ResponseEntity<>(languages, HttpStatus.OK);

	}

}
