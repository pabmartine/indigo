package com.martinia.indigo.book.infrastructure.api.controllers.recommendation;

import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.FindBookRecommendationsByBookUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/book")
public class FindBookRecommendationsByBookController {

	@Resource
	private FindBookRecommendationsByBookUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@GetMapping(value = "/recommendations/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getBookRecommendationsByBook(@RequestParam List<String> recommendations,
			@RequestParam List<String> languages) {
		List<Book> books = useCase.getRecommendationsByBook(recommendations, languages);
		List<BookDto> booksDto = mapper.domains2Dtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

}
