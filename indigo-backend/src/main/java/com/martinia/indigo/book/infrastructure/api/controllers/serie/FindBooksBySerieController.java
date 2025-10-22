package com.martinia.indigo.book.infrastructure.api.controllers.serie;

import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.serie.FindBooksBySerieUseCase;
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
public class FindBooksBySerieController {

	@Resource
	private FindBooksBySerieUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@Operation(summary = "Find books by series",
			description = "Retrieves a list of books belonging to a specific series, optionally filtered by language.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the list of books in the series",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = BookDto.class)))
			})
	@GetMapping(value = "/serie", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getSerie(
			@Parameter(description = "The name of the series to retrieve books from", example = "The Dark Tower") @RequestParam String serie,
			@Parameter(description = "List of languages to filter books by (e.g., 'en', 'es')", example = "[\"en\"]") @RequestParam List<String> languages) {
		List<Book> books = useCase.getSerie(serie, languages);
		List<BookDto> booksDto = mapper.domains2Dtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

}
