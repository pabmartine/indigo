package com.martinia.indigo.controllers.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

	@GetMapping("/count")
	public long getTotal() {
		return tagService.count();
	}

	@GetMapping(value = "/numbooks", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, String>> getNumBooksByTag(@RequestParam String sort, @RequestParam String order) {
		List<Object[]> data = tagService.getNumBooksByTag(sort, order);

		List<Map<String, String>> ret = new ArrayList<>(data.size());
		for (Object[] o : data) {
			Map<String, String> map = new HashMap<>();
			map.put("id", o[0].toString());
			map.put("tag", o[1].toString());
			map.put("total", o[2].toString());
			ret.add(map);
		}
		return ret;
	}
	//TODO MAPPING
	@GetMapping(value = "/tag", produces = MediaType.APPLICATION_JSON_VALUE)
	public Tag getTagByName(@RequestParam String name) {
		return tagService.getTagByName(name);
	}

	@GetMapping(value = "/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getTagsByBookId(@RequestParam int id) {
		return tagService.getTagsByBookId(id);
	}

	@GetMapping("/rename")
	public void rename(@RequestParam int source, @RequestParam String target) {
		Optional<Tag> optional = tagService.findById(source);
		if (optional.isPresent()) {
			Tag tag = optional.get();
			tag.setName(target);
			tagService.save(tag);
		}
	}

	@Transactional
	@GetMapping("/merge")
	public void merge(@RequestParam int source, @RequestParam int target) {
		Optional<Tag> tagSource = tagService.findById(source);
		Optional<Tag> tagTarget = tagService.findById(target);
		tagService.updateAllBookReferences(tagSource.get()
				.getId(),
				tagTarget.get()
						.getId());
		tagService.delete(tagSource.get());
	}

	@GetMapping("/image")
	public void image(@RequestParam int source, @RequestParam String image) {
		Optional<MyTag> optional = myTagService.findById(source);
		MyTag tag = null;
		if (optional.isPresent()) {
			tag = optional.get();
			tag.setImage(image);
		} else {
			tag = new MyTag(source, image);
		}
		myTagService.save(tag);
	}
	//TODO MAPPING
	@GetMapping("/image/id")
	public Optional<MyTag> getImage(@RequestParam int source) {
		return myTagService.findById(source);
	}
}
