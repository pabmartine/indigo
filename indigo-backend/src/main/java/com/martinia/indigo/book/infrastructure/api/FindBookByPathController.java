package com.martinia.indigo.book.infrastructure.api;

import com.martinia.indigo.book.infrastructure.model.BookDto;
import com.martinia.indigo.book.infrastructure.mapper.BookDtoMapper;
import com.martinia.indigo.book.domain.service.FindBookByPathUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/book")
public class FindBookByPathController {

	@Resource
	private FindBookByPathUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@GetMapping(value = "/path", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookDto> getBookByPath(@RequestParam String path) {
		BookDto bookDto = useCase.findByPath(path).map(book -> mapper.domain2Dto(book)).orElse(null);
		return new ResponseEntity<>(bookDto, HttpStatus.OK);
	}

}
