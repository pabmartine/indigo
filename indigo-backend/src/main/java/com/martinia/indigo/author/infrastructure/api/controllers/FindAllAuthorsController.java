package com.martinia.indigo.author.infrastructure.api.controllers;

import com.martinia.indigo.author.infrastructure.api.model.AuthorDto;
import com.martinia.indigo.author.infrastructure.api.model.AuthorSummaryDto;
import com.martinia.indigo.author.infrastructure.api.model.AuthorSummaryPageDto;
import com.martinia.indigo.author.infrastructure.api.mappers.AuthorDtoMapper;
import com.martinia.indigo.author.infrastructure.api.mappers.AuthorSummaryDtoMapper;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.model.AuthorPageData;
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

	@Resource
	private AuthorSummaryDtoMapper summaryMapper;

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

	@Operation(summary = "Find all authors summary with total",
			description = "Retrieves a paginated lightweight list of authors together with the total number of matching authors.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the lightweight list of authors with total",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = AuthorSummaryPageDto.class)))
			})
	@GetMapping(value = "/summary/page", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AuthorSummaryPageDto> getAuthorsSummaryPage(
			@Parameter(description = "List of languages to filter authors by (e.g., 'en', 'es')", example = "en") @RequestParam(required = false) List<String> languages,
			@Parameter(description = "Page number for pagination (0-indexed)", example = "0") @RequestParam int page,
			@Parameter(description = "Number of authors per page", example = "20") @RequestParam int size,
			@Parameter(description = "Field to sort by (e.g., 'name')", example = "name") @RequestParam String sort,
			@Parameter(description = "Sort order (asc or desc)", example = "asc") @RequestParam String order) {
		AuthorPageData authorsPage = useCase.findSummaryPage(languages != null ? languages : List.of(), page, size, sort, order);
		List<AuthorSummaryDto> authorsDto = summaryMapper.domains2Dtos(authorsPage.items());
		return new ResponseEntity<>(new AuthorSummaryPageDto(authorsDto, authorsPage.total(), page, size), HttpStatus.OK);
	}

}
