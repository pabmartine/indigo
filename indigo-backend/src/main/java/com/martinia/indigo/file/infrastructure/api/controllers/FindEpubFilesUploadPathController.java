package com.martinia.indigo.file.infrastructure.api.controllers;

import com.martinia.indigo.file.domain.ports.usecases.FindEpubFilesUploadPathUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
@Tag(name = "Files", description = "API for file management")
public class FindEpubFilesUploadPathController {

	@Resource
	private FindEpubFilesUploadPathUseCase useCase;

	@Operation(summary = "Find EPUB files upload path",
			description = "Retrieves the configured upload path for EPUB files.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the upload path",
							content = @Content(mediaType = "application/json",
									schema = @Schema(type = "object", example = "{\"path\": \"/path/to/uploads\"}")))
			})
	@GetMapping(value = "/path", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> findPath() {
		Map<String, String> map = new HashMap<>();
		final String path = useCase.findPath();
		map.put("path", path);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

}
