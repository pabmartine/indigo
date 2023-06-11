package com.martinia.indigo.book.infrastructure.api;

import com.martinia.indigo.adapters.in.rest.dtos.BookDto;
import com.martinia.indigo.adapters.in.rest.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.service.FindBookByIdUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/book")
public class FindBookByIdController {

	@Autowired
	private FindBookByIdUseCase useCase;

	@Autowired
	protected BookDtoMapper mapper;

	@GetMapping(value = "/id", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookDto> getBookById(@RequestParam String id) {
		BookDto bookDto = useCase.findById(id).map(book -> mapper.domain2Dto(book)).orElse(null);
		return new ResponseEntity<>(bookDto, HttpStatus.OK);
	}

}
