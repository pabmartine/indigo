package com.martinia.indigo.book.infrastructure.api.favorite;

import com.martinia.indigo.adapters.in.rest.dtos.BookDto;
import com.martinia.indigo.adapters.in.rest.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.service.favorite.FindFavoriteBooksUseCase;
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
@RequestMapping("/rest/book")
public class FindFavoriteBooksController {

	@Resource
	private FindFavoriteBooksUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BookDto>> getFavoriteBooks(@RequestParam String user) {
		List<Book> books = useCase.getFavoriteBooks(user);
		List<BookDto> booksDto = mapper.domains2Dtos(books);
		return new ResponseEntity<>(booksDto, HttpStatus.OK);

	}

}
