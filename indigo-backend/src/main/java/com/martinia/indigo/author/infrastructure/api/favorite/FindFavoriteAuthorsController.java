package com.martinia.indigo.author.infrastructure.api.favorite;

import com.martinia.indigo.author.infrastructure.model.AuthorDto;
import com.martinia.indigo.author.infrastructure.mapper.AuthorDtoMapper;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.usecases.favorite.FindFavoriteAuthorsUseCase;
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
public class FindFavoriteAuthorsController {

	@Resource
	private FindFavoriteAuthorsUseCase useCase;

	@Resource
	protected AuthorDtoMapper mapper;

	@GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<AuthorDto>> getFavoriteAuthors(@RequestParam String user) {
		List<Author> authors = useCase.getFavoriteAuthors(user);
		List<AuthorDto> authorsDto = mapper.domains2Dtos(authors);
		return new ResponseEntity<>(authorsDto, HttpStatus.OK);
	}

}
