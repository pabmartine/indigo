package com.martinia.indigo.tag.infrastructure.api;

import com.martinia.indigo.adapters.in.rest.dtos.TagDto;
import com.martinia.indigo.adapters.in.rest.mappers.TagDtoMapper;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.service.FindAllTagsUseCase;
import com.martinia.indigo.tag.domain.service.FindTagByNameUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/tag")
public class FindAllTagsController {

	@Autowired
	private FindAllTagsUseCase useCase;

	@Autowired
	protected TagDtoMapper mapper;

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TagDto>> getAll(@RequestParam final List<String> languages, final @RequestParam String sort,
			@RequestParam String order) {
		final List<Tag> tags = useCase.findAll(languages, sort, order);
		final List<TagDto> tagsDto = mapper.domains2Dtos(tags);
		return new ResponseEntity<>(tagsDto, HttpStatus.OK);
	}

}
