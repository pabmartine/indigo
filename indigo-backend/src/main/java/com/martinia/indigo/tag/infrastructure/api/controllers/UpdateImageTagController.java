package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.tag.domain.ports.usecases.UpdateImageTagUseCase;
import com.martinia.indigo.tag.infrastructure.api.mappers.TagDtoMapper;
import com.martinia.indigo.tag.infrastructure.api.model.TagDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Optional;

@RestController
@RequestMapping("/api/tag")
public class UpdateImageTagController {

	@Resource
	private UpdateImageTagUseCase useCase;

	@Resource
	private TagDtoMapper mapper;

	@Transactional
	@GetMapping(value = "/image/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TagDto> updateImage(@RequestParam final String source) {
		final TagDto tagDto = Optional.ofNullable(useCase.updateImage(source)).map(tag -> mapper.domain2Dto(tag)).orElse(null);
		return new ResponseEntity<>(tagDto, HttpStatus.OK);
	}

}
