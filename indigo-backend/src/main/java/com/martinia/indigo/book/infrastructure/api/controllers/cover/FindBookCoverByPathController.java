package com.martinia.indigo.book.infrastructure.api.controllers.cover;

import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.ports.usecases.cover.FindBookCoverByPathUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API for book management")
public class FindBookCoverByPathController {

	@Resource
	private FindBookCoverByPathUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@Operation(summary = "Get book cover image by path",
			description = "Retrieves the base64 encoded cover image of a book given its file path.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the book cover image",
							content = @Content(mediaType = "application/json",
									schema = @Schema(type = "object", example = "{\"image\": \"base64encodedstring...\"}"))),
					@ApiResponse(responseCode = "404", description = "Book cover not found")
			})
	@GetMapping(value = "/image", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> getImage(
			@Parameter(description = "The file path of the book to retrieve its cover", example = "/books/my_book.epub") @RequestParam String path) {
		Map<String, String> map = null;

		Optional<String> image = useCase.getImage(path);

		if (image.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		map = new HashMap<String, String>();
		map.put("image", image.get());
		return ResponseEntity.status(HttpStatus.OK)
				.cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic())
				.contentType(MediaType.APPLICATION_JSON)
				.body(map);

	}
}
