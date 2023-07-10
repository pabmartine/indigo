package com.martinia.indigo.author.infrastructure.api;

import com.martinia.indigo.author.infrastructure.model.AuthorDto;
import com.martinia.indigo.author.infrastructure.mapper.AuthorDtoMapper;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.usecases.FindAllAuthorsUseCase;
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
@RequestMapping("/rest/author")
public class FindAllAuthorsController {

	@Resource
	private FindAllAuthorsUseCase useCase;

	@Resource
	protected AuthorDtoMapper mapper;

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<AuthorDto>> getAll(@RequestParam List<String> languages, @RequestParam int page, @RequestParam int size,
			@RequestParam String sort, @RequestParam String order) {
		List<Author> authors = useCase.findAll(languages, page, size, sort, order);
		List<AuthorDto> authorsDto = mapper.domains2Dtos(authors);
		return new ResponseEntity<>(authorsDto, HttpStatus.OK);
	}

}
