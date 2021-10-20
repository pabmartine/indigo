package com.martinia.indigo.adapters.in.rest.controllers;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.adapters.in.rest.dtos.TagDto;
import com.martinia.indigo.adapters.in.rest.mappers.TagDtoMapper;
import com.martinia.indigo.domain.model.Tag;
import com.martinia.indigo.ports.in.rest.TagService;

@RestController
@RequestMapping("/rest/tag")
public class TagRestController {

	@Autowired
	private TagService tagService;

	@Autowired
	protected TagDtoMapper tagDtoMapper;

//	@GetMapping("/count")
//	public ResponseEntity<Long> getTotal() {
//		return new ResponseEntity<>(tagService.count(), HttpStatus.OK);
//	}

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<TagDto>> getAll(@RequestParam String sort, @RequestParam String order) {
		List<Tag> tags = tagService.findAll(sort, order);
		List<TagDto> tagsDto = tagDtoMapper.domains2Dtos(tags);
		return new ResponseEntity<>(tagsDto, HttpStatus.OK);
	}

	@GetMapping(value = "/tag", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TagDto> getTagByName(@RequestParam String name) {
		Tag tag = tagService.findByName(name);
		TagDto tagDto = tagDtoMapper.domain2Dto(tag);
		return new ResponseEntity<>(tagDto, HttpStatus.OK);
	}

	@GetMapping("/rename")
	public ResponseEntity<Void> rename(@RequestParam String source, @RequestParam String target) {
		tagService.rename(source, target);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Transactional
	@GetMapping("/merge")
	public ResponseEntity<Void> merge(@RequestParam String source, @RequestParam String target) {
		tagService.merge(source, target);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/image")
	public ResponseEntity<Void> image(@RequestParam String source, @RequestParam String image) {
		tagService.image(source, image);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
