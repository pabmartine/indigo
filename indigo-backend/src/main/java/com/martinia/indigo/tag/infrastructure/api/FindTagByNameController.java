package com.martinia.indigo.tag.infrastructure.api;

import com.martinia.indigo.tag.infrastructure.model.TagDto;
import com.martinia.indigo.tag.infrastructure.mapper.TagDtoMapper;
import com.martinia.indigo.tag.domain.ports.usecases.FindTagByNameUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/tag")
public class FindTagByNameController {

	@Resource
	private FindTagByNameUseCase useCase;

	@Resource
	private TagDtoMapper mapper;

	@GetMapping(value = "/tag", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TagDto> getTagByName(@RequestParam final String name) {
		final TagDto tagDto = useCase.findByName(name).map(tag -> mapper.domain2Dto(tag)).orElse(null);
		return new ResponseEntity<>(tagDto, HttpStatus.OK);
	}

}
