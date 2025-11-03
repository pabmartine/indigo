package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.tag.infrastructure.api.model.TagDto;
import com.martinia.indigo.tag.infrastructure.api.mappers.TagDtoMapper;
import com.martinia.indigo.tag.domain.ports.usecases.FindAllTagsUseCase;
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
@RequestMapping("/api/tag")
@Tag(name = "Tags", description = "API for managing book tags")
public class FindAllTagsController {

	@Resource
	private FindAllTagsUseCase useCase;

	@Resource
	private TagDtoMapper mapper;

	@Operation(summary = "Find all tags",
			description = "Retrieves a list of all tags, optionally filtered by language and sorted.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the list of tags",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = TagDto.class)))
			})
	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TagDto>> getAll(
			@Parameter(description = "List of languages to filter tags by (e.g., 'en', 'es')", example = "[\"en\"]") @RequestParam final List<String> languages,
			@Parameter(description = "Field to sort by (e.g., 'name')", example = "name") @RequestParam String sort,
			@Parameter(description = "Sort order (asc or desc)", example = "asc") @RequestParam String order) {
		final List<com.martinia.indigo.tag.domain.model.Tag> tags = useCase.findAll(languages, sort, order);
		final List<TagDto> tagsDto = mapper.domains2Dtos(tags);
		return new ResponseEntity<>(tagsDto, HttpStatus.OK);
	}

}

