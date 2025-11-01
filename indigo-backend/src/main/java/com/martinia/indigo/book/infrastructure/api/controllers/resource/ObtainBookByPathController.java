package com.martinia.indigo.book.infrastructure.api.controllers.resource;

import com.martinia.indigo.book.domain.ports.usecases.resource.ObtainBookByPathUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API for book management")
public class ObtainBookByPathController {

	@Resource
	private ObtainBookByPathUseCase useCase;

	@Operation(summary = "Obtain book by path",
			description = "Retrieves an EPUB book file by its file path.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the EPUB file",
							content = @Content(mediaType = "application/epub+zip",
									schema = @Schema(type = "string", format = "binary"))),
					@ApiResponse(responseCode = "404", description = "Book not found at the specified path")
			})
	@GetMapping(value = "/epub")
	@ResponseBody
	public ResponseEntity<org.springframework.core.io.Resource> getEpub(
			@Parameter(description = "The file path of the EPUB book to retrieve", example = "/books/my_book.epub") @RequestParam String path) throws IOException {

		org.springframework.core.io.Resource epub = useCase.getEpub(path);

		if (epub == null || !epub.exists()) {
			return ResponseEntity.notFound().build();
		}

		String contentType = Files.probeContentType(epub.getFile().toPath());
		if (contentType == null) {
			contentType = "application/epub+zip";
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + epub.getFilename() + "\"")
				.body(epub);

	}

}
