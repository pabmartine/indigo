package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.tag.domain.ports.usecases.MergeTagUseCase;
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
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/tag")
@Tag(name = "Tags", description = "API for managing book tags")
public class MergeTagController {

	@Resource
	private MergeTagUseCase useCase;

	@Transactional
	@Operation(summary = "Merge tags",
			description = "Merges a source tag into a target tag, effectively replacing all occurrences of the source tag with the target tag.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Tags merged successfully"),
					@ApiResponse(responseCode = "400", description = "Invalid source or target tag supplied")
			})
	@GetMapping("/merge")
	public ResponseEntity<Void> merge(
			@Parameter(description = "The name of the tag to be merged (source)", example = "OldTag") @RequestParam final String source,
			@Parameter(description = "The name of the tag to merge into (target)", example = "NewTag") @RequestParam final String target) {
		useCase.merge(source, target);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
