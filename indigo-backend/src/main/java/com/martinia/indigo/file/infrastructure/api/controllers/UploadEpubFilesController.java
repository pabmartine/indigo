package com.martinia.indigo.file.infrastructure.api.controllers;

import com.martinia.indigo.file.domain.ports.usecases.UploadEpubFilesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/file")
@Tag(name = "Files", description = "API for file management")
public class UploadEpubFilesController {

	@Resource
	private UploadEpubFilesUseCase useCase;

	@Operation(summary = "Upload EPUB files",
			description = "Initiates the upload process for a specified number of EPUB files.",
			responses = {
					@ApiResponse(responseCode = "200", description = "EPUB files upload initiated successfully"),
					@ApiResponse(responseCode = "400", description = "Invalid number of files supplied")
			})
	@PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> upload(
			@Parameter(description = "The number of EPUB files to upload", example = "10") @RequestParam Long number) {
		if (number == null || number <= 0) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		useCase.upload(number);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
