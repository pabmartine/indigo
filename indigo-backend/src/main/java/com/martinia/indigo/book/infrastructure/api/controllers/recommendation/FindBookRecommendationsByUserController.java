package com.martinia.indigo.book.infrastructure.api.controllers.recommendation;

import org.springframework.security.core.Authentication;

import com.martinia.indigo.book.infrastructure.api.mappers.BookSummaryDtoMapper;

import com.martinia.indigo.book.infrastructure.api.model.BookSummaryPageDto;

import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.FindBookRecommendationsByUserUseCase;
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
public class FindBookRecommendationsByUserController {

	@Resource
	private FindBookRecommendationsByUserUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@Resource
	private BookSummaryDtoMapper summaryMapper;

	@GetMapping(value = "/recommendations/user/summary/page", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookSummaryPageDto> getSummaryPage(
			@RequestParam String user, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "count") String sort,
			@RequestParam(defaultValue = "desc") String order,
			Authentication authentication) {
		if (authentication == null || !user.equals(authentication.getName())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		if (page < 0 || size < 1 || size > 200) {
			return ResponseEntity.badRequest().build();
		}
		var result = useCase.getSummaryPage(user, page, size, sort, order);
		return ResponseEntity.ok(new BookSummaryPageDto(
				summaryMapper.domains2Dtos(result.items()), result.total(), page, size));
	}

	@Operation(summary = "Get book recommendations by user",
			description = "Retrieves a paginated and sortable list of recommended books for a specific user.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the list of recommended books",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = BookDto.class)))
			})
	@GetMapping(value = "/recommendations/user", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getBookRecommendationsByUser(
			@Parameter(description = "The username of the user to retrieve recommendations for", example = "john.doe") @RequestParam String user,
			@Parameter(description = "Page number for pagination (0-indexed)", example = "0") @RequestParam int page,
			@Parameter(description = "Number of books per page", example = "10") @RequestParam int size,
			@Parameter(description = "Field to sort by (e.g., 'title')", example = "title") @RequestParam String sort,
			@Parameter(description = "Sort order (asc or desc)", example = "asc") @RequestParam String order) {
		List<Book> books = useCase.getRecommendationsByUser(user, page, size, sort, order);
		List<BookDto> booksDto = mapper.domains2Dtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

}
