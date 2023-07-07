package com.martinia.indigo.metadata.infrastructure.api;

import com.martinia.indigo.book.infrastructure.model.BookDto;
import com.martinia.indigo.book.infrastructure.mapper.BookDtoMapper;
import com.martinia.indigo.metadata.domain.service.RefreshBookMetadataUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/metadata")
public class RefreshBookMetadataController {

	@Resource
	private RefreshBookMetadataUseCase useCase;

	@Resource
	protected BookDtoMapper mapper;

	@GetMapping(value = "/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookDto> refreshBook(@RequestParam String book) {
		BookDto bookDto = useCase.findBookMetadata(book).map(_book -> mapper.domain2Dto(_book)).orElse(null);
		return new ResponseEntity<>(bookDto, HttpStatus.OK);
	}

}
