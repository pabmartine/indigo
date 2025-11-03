package com.martinia.indigo.book.infrastructure.api.controllers.favorite;

import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.favorite.FindFavoriteBooksUseCase;
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
public class FindFavoriteBooksController {

	@Resource
	private FindFavoriteBooksUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@Operation(summary = "Find favorite books",
			description = "Retrieves a list of favorite books for a given user.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the list of favorite books",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = BookDto.class)))
			})
	@GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getFavoriteBooks(
			@Parameter(description = "The username of the user", example = "john.doe") @RequestParam String user) {
		List<Book> books = useCase.getFavoriteBooks(user);
		List<BookDto> booksDto = mapper.domains2Dtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

}
