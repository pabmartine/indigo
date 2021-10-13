package com.martinia.indigo.controllers.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.dto.TagDto;
import com.martinia.indigo.mappers.TagDtoMapper;
import com.martinia.indigo.model.calibre.Tag;
import com.martinia.indigo.model.indigo.MyTag;
import com.martinia.indigo.services.calibre.TagService;
import com.martinia.indigo.services.indigo.MyTagService;

@RestController
@RequestMapping("/rest/tag")
public class TagRestController {

	@Autowired
	private TagService tagService;

	@Autowired
	private MyTagService myTagService;

	@Autowired
	protected TagDtoMapper tagDtoMapper;

	@GetMapping("/count")
	public ResponseEntity<Long> getTotal() {
		return new ResponseEntity<>(tagService.count(), HttpStatus.OK);
	}

	@GetMapping(value = "/numbooks", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Map<String, String>>> getNumBooksByTag(@RequestParam String sort,
			@RequestParam String order) {

		List<Object[]> data = tagService.getNumBooksByTag(sort, order);

		List<Map<String, String>> ret = new ArrayList<>(data.size());
		for (Object[] o : data) {
			Map<String, String> map = new HashMap<>();
			map.put("id", o[0].toString());
			map.put("tag", o[1].toString());
			map.put("total", o[2].toString());
			ret.add(map);
		}

		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

	@GetMapping(value = "/tag", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TagDto> getTagByName(@RequestParam String name) {
		Tag tag = tagService.getTagByName(name);
		TagDto tagDto = tagDtoMapper.tagToTagDto(tag);
		return new ResponseEntity<>(tagDto, HttpStatus.OK);
	}

	@GetMapping(value = "/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> getTagsByBookId(@RequestParam int id) {
		return new ResponseEntity<>(tagService.getTagsByBookId(id), HttpStatus.OK);
	}

	@GetMapping("/rename")
	public ResponseEntity<Void> rename(@RequestParam int source, @RequestParam String target) {
		tagService.rename(source, target);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Transactional
	@GetMapping("/merge")
	public ResponseEntity<Void> merge(@RequestParam int source, @RequestParam int target) {
		tagService.merge(source, target);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/image")
	public ResponseEntity<Void> image(@RequestParam int source, @RequestParam String image) {
		myTagService.image(source, image);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/image/id")
	public ResponseEntity<TagDto> getImage(@RequestParam int source) {
		MyTag myTag = myTagService.findById(source)
				.orElse(null);
		TagDto tagDto = tagDtoMapper.myTagToTagDto(myTag);
		return new ResponseEntity<>(tagDto, HttpStatus.OK);
	}
}
