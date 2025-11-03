package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.tag.domain.ports.usecases.RenameTagUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/tag")
@Tag(name = "Tags", description = "API for managing book tags")
public class RenameTagController {

	@Resource
	private RenameTagUseCase useCase;

	@Operation(summary = "Rename tag",
			description = "Renames an existing tag from a source name to a target name.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Tag renamed successfully"),
					@ApiResponse(responseCode = "400", description = "Invalid source or target tag name supplied")
			})
	@GetMapping("/rename")
	public ResponseEntity<Void> rename(
			@Parameter(description = "The current name of the tag (source)", example = "OldName") @RequestParam final String source,
			@Parameter(description = "The new name for the tag (target)", example = "NewName") @RequestParam final String target) {
		useCase.rename(source, target);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
