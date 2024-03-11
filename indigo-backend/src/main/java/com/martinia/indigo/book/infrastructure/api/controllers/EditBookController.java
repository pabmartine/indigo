package com.martinia.indigo.book.infrastructure.api.controllers;

import com.martinia.indigo.book.domain.ports.usecases.EditBookUseCase;
import com.martinia.indigo.book.infrastructure.api.mappers.BookDtoMapper;
import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/book")
public class EditBookController {
	@Resource
	private EditBookUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@PutMapping(value = "/edit")
	public ResponseEntity edit(@Valid @RequestBody BookDto bookDto) {
		useCase.edit(mapper.dto2domain(bookDto));
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
