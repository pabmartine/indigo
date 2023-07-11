package com.martinia.indigo.author.infrastructure.api.controllers;

import com.martinia.indigo.author.infrastructure.api.model.AuthorDto;
import com.martinia.indigo.author.infrastructure.api.mappers.AuthorDtoMapper;
import com.martinia.indigo.author.domain.ports.usecases.FindAuthorsSortByNameUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/author")
public class FindAuthorsSortByNameController {

	@Resource
	private FindAuthorsSortByNameUseCase useCase;

	@Resource
	protected AuthorDtoMapper mapper;

	@GetMapping(value = "/sort", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AuthorDto> findBySortName(@RequestParam String sort) {

		AuthorDto authorDto = useCase.findBySort(sort).map(author -> mapper.domain2Dto(author)).orElse(null);
		return new ResponseEntity<>(authorDto, HttpStatus.OK);
	}

}
