package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.tag.domain.ports.usecases.UpdateImageTagUseCase;
import com.martinia.indigo.tag.infrastructure.api.mappers.TagDtoMapper;
import com.martinia.indigo.tag.infrastructure.api.model.TagDto;
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
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@RestController
@RequestMapping("/api/tag")
@Tag(name = "Tags", description = "API for managing book tags")
public class UpdateImageTagController {

	@Resource
	private UpdateImageTagUseCase useCase;

	@Resource
	private TagDtoMapper mapper;

	@Transactional
	@Operation(summary = "Update tag image",
			description = "Updates the image associated with a specific tag.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Tag image updated successfully",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = TagDto.class))),
					@ApiResponse(responseCode = "404", description = "Tag not found")
			})
	@GetMapping(value = "/image/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TagDto> updateImage(
			@Parameter(description = "The name of the tag to update its image", example = "Fiction") @RequestParam final String source) {
		final TagDto tagDto = Optional.ofNullable(useCase.updateImage(source)).map(tag -> mapper.domain2Dto(tag)).orElse(null);
		return new ResponseEntity<>(tagDto, HttpStatus.OK);
	}

}
