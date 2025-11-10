package com.martinia.indigo.serie.infrastructure.api.controllers;

import com.martinia.indigo.serie.domain.ports.usecases.FindCoverSerieUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/serie")
@Tag(name = "Series", description = "API for managing book series")
public class FindCoverSerieController {

	@Resource
	private FindCoverSerieUseCase useCase;

	@Operation(summary = "Get series cover image",
			description = "Returns the cover image for a given series.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Cover found"),
					@ApiResponse(responseCode = "404", description = "Cover not found")
			})
	@GetMapping(value = "/cover", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getCover(
			@Parameter(description = "Name of the series", example = "The Dark Tower") @RequestParam String serie) {

		byte[] cover = useCase.getCover(serie);
		if (cover == null || cover.length == 0) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok()
				.cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
				.contentType(MediaType.IMAGE_JPEG)
				.body(cover);
	}
}
