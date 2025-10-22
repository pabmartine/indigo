package com.martinia.indigo.author.infrastructure.api.controllers;

import com.martinia.indigo.author.infrastructure.api.model.AuthorDto;
import com.martinia.indigo.author.infrastructure.api.mappers.AuthorDtoMapper;
import com.martinia.indigo.author.domain.ports.usecases.FindAuthorsSortByNameUseCase;
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

@RestController
@RequestMapping("/api/author")
@Tag(name = "Authors", description = "API for author management")
public class FindAuthorsSortByNameController {

	@Resource
	private FindAuthorsSortByNameUseCase useCase;

	@Resource
	protected AuthorDtoMapper mapper;

	@Operation(summary = "Find author by sort name",
			description = "Retrieves an author by their sortable name.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the author",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = AuthorDto.class))),
					@ApiResponse(responseCode = "404", description = "Author not found")
			})
	@GetMapping(value = "/sort", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AuthorDto> findBySortName(
			@Parameter(description = "The sortable name of the author to retrieve", example = "Doe, John") @RequestParam String sort) {

		AuthorDto authorDto = useCase.findBySort(sort).map(author -> mapper.domain2Dto(author)).orElse(null);
		return new ResponseEntity<>(authorDto, HttpStatus.OK);
	}

}
