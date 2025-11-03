package com.martinia.indigo.book.infrastructure.api.controllers.recommendation;

import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.FindBookRecommendationsByBookUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API for book management")
public class FindBookRecommendationsByBookController {

	@Resource
	private FindBookRecommendationsByBookUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@Operation(summary = "Get book recommendations by book",
			description = "Retrieves a list of recommended books based on a given list of book IDs and languages.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the list of recommended books",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = BookDto.class)))
			})
	@GetMapping(value = "/recommendations/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getBookRecommendationsByBook(
			@Parameter(description = "List of book IDs to base recommendations on", example = "[\"book1\", \"book2\"]") @RequestParam List<String> recommendations,
			@Parameter(description = "List of languages to filter recommendations by (e.g., 'en', 'es')", example = "[\"en\"]") @RequestParam List<String> languages) {
		List<Book> books = useCase.getRecommendationsByBook(recommendations, languages);
		List<BookDto> booksDto = mapper.domains2Dtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

}
