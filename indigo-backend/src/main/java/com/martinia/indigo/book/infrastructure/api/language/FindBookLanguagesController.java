package com.martinia.indigo.book.infrastructure.api.language;

import com.martinia.indigo.book.domain.ports.usecases.language.FindBookLanguagesUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/rest/book")
public class FindBookLanguagesController {

	@Resource
	private FindBookLanguagesUseCase useCase;

	@GetMapping(value = "/languages", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> getBookLanguages() {

		List<String> languages = useCase.getBookLanguages();
		return new ResponseEntity<>(languages, HttpStatus.OK);

	}

}
