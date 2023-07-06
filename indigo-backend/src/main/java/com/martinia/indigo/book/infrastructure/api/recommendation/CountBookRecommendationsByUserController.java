package com.martinia.indigo.book.infrastructure.api.recommendation;

import com.martinia.indigo.adapters.in.rest.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.service.recommendation.CountBookRecommendationsByUserUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/book")
public class CountBookRecommendationsByUserController {

	@Resource
	private CountBookRecommendationsByUserUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	//Todo ver como filtrar estos libros también por idioma
	@GetMapping(value = "/recommendations/user/count")
	public ResponseEntity<Long> countBookRecommendationsByUser(@RequestParam String user) {
		return new ResponseEntity<>(useCase.countRecommendationsByUser(user), HttpStatus.OK);
	}

}
