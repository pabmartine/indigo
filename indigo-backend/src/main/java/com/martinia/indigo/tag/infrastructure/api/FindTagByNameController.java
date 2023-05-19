package com.martinia.indigo.tag.infrastructure.api;

import com.martinia.indigo.adapters.in.rest.dtos.TagDto;
import com.martinia.indigo.adapters.in.rest.mappers.TagDtoMapper;
import com.martinia.indigo.tag.domain.service.FindTagByNameUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/tag")
public class FindTagByNameController {

	@Autowired
	private FindTagByNameUseCase useCase;

	@Autowired
	protected TagDtoMapper mapper;

	@GetMapping(value = "/tag", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TagDto> getTagByName(@RequestParam final String name) {
		final TagDto tagDto = useCase.findByName(name).map(tag -> mapper.domain2Dto(tag)).orElse(null);
		return new ResponseEntity<>(tagDto, HttpStatus.OK);
	}

}
