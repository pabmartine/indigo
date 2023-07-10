package com.martinia.indigo.tag.infrastructure.api;

import com.martinia.indigo.tag.infrastructure.model.TagDto;
import com.martinia.indigo.tag.infrastructure.mapper.TagDtoMapper;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.usecases.FindAllTagsUseCase;
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
@RequestMapping("/api/tag")
public class FindAllTagsController {

	@Resource
	private FindAllTagsUseCase useCase;

	@Resource
	private TagDtoMapper mapper;

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TagDto>> getAll(@RequestParam final List<String> languages, final @RequestParam String sort,
			@RequestParam String order) {
		final List<Tag> tags = useCase.findAll(languages, sort, order);
		final List<TagDto> tagsDto = mapper.domains2Dtos(tags);
		return new ResponseEntity<>(tagsDto, HttpStatus.OK);
	}

}
