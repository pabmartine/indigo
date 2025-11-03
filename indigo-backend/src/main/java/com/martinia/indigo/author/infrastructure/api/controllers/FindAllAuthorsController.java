package com.martinia.indigo.author.infrastructure.api.controllers;

import com.martinia.indigo.author.infrastructure.api.model.AuthorDto;
import com.martinia.indigo.author.infrastructure.api.mappers.AuthorDtoMapper;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.usecases.FindAllAuthorsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/author")
@Tag(name = "Authors", description = "API for author management")
public class FindAllAuthorsController {

	@Resource
	private FindAllAuthorsUseCase useCase;

	@Resource
	protected AuthorDtoMapper mapper;

	@Operation(summary = "Find all authors",
			description = "Retrieves a paginated and sortable list of authors, optionally filtered by language.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the list of authors",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = AuthorDto.class)))
			})
	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<AuthorDto>> getAll(
			@Parameter(description = "List of languages to filter authors by (e.g., 'en', 'es')", example = "en") @RequestParam List<String> languages,
			@Parameter(description = "Page number for pagination (0-indexed)", example = "0") @RequestParam int page,
			@Parameter(description = "Number of authors per page", example = "10") @RequestParam int size,
			@Parameter(description = "Field to sort by (e.g., 'name')", example = "name") @RequestParam String sort,
			@Parameter(description = "Sort order (asc or desc)", example = "asc") @RequestParam String order) {
		List<Author> authors = useCase.findAll(languages, page, size, sort, order);
		List<AuthorDto> authorsDto = mapper.domains2Dtos(authors);
		return new ResponseEntity<>(authorsDto, HttpStatus.OK);
	}

}
