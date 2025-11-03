package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.tag.domain.ports.usecases.SetTagImageUseCase;
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
public class SetTagImageController {

	@Resource
	private SetTagImageUseCase useCase;

	@Operation(summary = "Set tag image",
			description = "Sets the image for a specific tag.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Tag image set successfully"),
					@ApiResponse(responseCode = "400", description = "Invalid tag name or image path supplied")
			})
	@GetMapping("/image")
	public ResponseEntity<Void> setImage(
			@Parameter(description = "The name of the tag to set the image for", example = "Fiction") @RequestParam final String source,
			@Parameter(description = "The path to the image file", example = "/images/fiction.jpg") @RequestParam final String image) {
		useCase.setImage(source, image);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
