package com.martinia.indigo.book.infrastructure.api.controllers.favorite;

import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.ports.usecases.favorite.CheckIsFavoriteBookUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/book")
public class CheckIsFavoriteBookController {

	@Resource
	private CheckIsFavoriteBookUseCase useCase;

	@Resource
	protected BookDtoMapper mapper;

	@GetMapping(value = "/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> isFavoriteBook(@RequestParam String book, @RequestParam String user) {
		Boolean isFavorite = useCase.isFavoriteBook(user, book);
		return new ResponseEntity<>(isFavorite, HttpStatus.OK);
	}

}
