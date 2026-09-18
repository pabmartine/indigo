package com.martinia.indigo.tag.infrastructure.api.controllers.cover;

import com.martinia.indigo.tag.domain.model.TagCoverResult;
import com.martinia.indigo.tag.domain.ports.usecases.cover.FindTagCoverByIdUseCase;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/tag")
@Tag(name = "Tags", description = "API for managing book tags")
public class FindTagCoverByIdController {

	@Resource
	private FindTagCoverByIdUseCase useCase;

	@Operation(summary = "Get tag cover image",
			description = "Returns the cover image for a given tag ID.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Cover found"),
					@ApiResponse(responseCode = "302", description = "Redirect to external image"),
					@ApiResponse(responseCode = "404", description = "Cover not found")
			})
	@GetMapping("/cover/{id}")
	public ResponseEntity<byte[]> getCover(
			@Parameter(description = "Tag ID", example = "60c72b2f9b1d8b2bad7b1234") @PathVariable String id) {

		TagCoverResult result = useCase.getCover(id);
		if (result == null || result.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		if (result.isRedirect()) {
			return ResponseEntity.status(HttpStatus.FOUND)
					.location(result.getRedirectUri())
					.build();
		}

		return ResponseEntity.ok()
				.cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
				.contentType(MediaType.IMAGE_JPEG)
				.body(result.getBytes());
	}
}
