package com.martinia.indigo.book.infrastructure.api.controllers.cover;

import com.martinia.indigo.book.domain.ports.usecases.cover.FindBookCoverByIdUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API for book management")
public class FindBookCoverByIdController {

	@Resource
	private FindBookCoverByIdUseCase useCase;

	@Operation(summary = "Get book cover image by book id",
			description = "Returns the binary cover image associated with the provided book id.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Cover found",
							content = @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary"))),
					@ApiResponse(responseCode = "404", description = "Cover not found")
			})
	@GetMapping(value = "/cover/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getCover(@Parameter(description = "Mongo identifier of the book") @PathVariable("id") String id) {
		return useCase.getCover(id)
				.map(bytes -> ResponseEntity.ok()
						.cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
						.contentType(MediaType.IMAGE_JPEG)
						.body(bytes))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
}
