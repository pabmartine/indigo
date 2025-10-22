package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.tag.infrastructure.api.model.TagDto;
import com.martinia.indigo.tag.infrastructure.api.mappers.TagDtoMapper;
import com.martinia.indigo.tag.domain.ports.usecases.FindTagByNameUseCase;
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
@RequestMapping("/api/tag")
@Tag(name = "Tags", description = "API for managing book tags")
public class FindTagByNameController {

	@Resource
	private FindTagByNameUseCase useCase;

	@Resource
	private TagDtoMapper mapper;

	@Operation(summary = "Find tag by name",
			description = "Retrieves a single tag by its name.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the tag",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = TagDto.class))),
					@ApiResponse(responseCode = "404", description = "Tag not found")
			})
	@GetMapping(value = "/tag", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TagDto> getTagByName(
			@Parameter(description = "The name of the tag to retrieve", example = "Fiction") @RequestParam final String name) {
		final TagDto tagDto = useCase.findByName(name).map(tag -> mapper.domain2Dto(tag)).orElse(null);
		return new ResponseEntity<>(tagDto, HttpStatus.OK);
	}

}
