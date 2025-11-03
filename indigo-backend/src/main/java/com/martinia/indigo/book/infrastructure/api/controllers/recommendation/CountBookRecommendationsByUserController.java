package com.martinia.indigo.book.infrastructure.api.controllers.recommendation;

import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.CountBookRecommendationsByUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API for book management")
public class CountBookRecommendationsByUserController {

	@Resource
	private CountBookRecommendationsByUserUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@Operation(summary = "Count book recommendations by user",
			description = "Retrieves the total number of book recommendations for a specific user.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved the count of recommendations",
							content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
			})
	@GetMapping(value = "/recommendations/user/count")
	public ResponseEntity<Long> countBookRecommendationsByUser(
			@Parameter(description = "The username of the user to count recommendations for", example = "john.doe") @RequestParam String user) {
		return new ResponseEntity<>(useCase.countRecommendationsByUser(user), HttpStatus.OK);
	}

}
