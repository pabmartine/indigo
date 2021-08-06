package com.martinia.indigo.rest;

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
import com.martinia.indigo.repository.calibre.TagRepository;
import com.martinia.indigo.repository.indigo.MyTagRepository;

@RestController
@RequestMapping("/rest/tag")
public class TagRestController {

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private MyTagRepository myTagRepository;

	@GetMapping("/count")
	public long getTotal() {
		return tagRepository.count();
	}

	@GetMapping(value = "/numbooks", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, String>> getNumBooksByTag(@RequestParam String sort, @RequestParam String order) {
		List<Object[]> data = tagRepository.getNumBooksByTag(sort, order);

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

	@GetMapping(value = "/tag", produces = MediaType.APPLICATION_JSON_VALUE)
	public Tag getTagByName(@RequestParam String name) {
		return tagRepository.getTagByName(name);
	}

	@GetMapping(value = "/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getTagsByBookId(@RequestParam int id) {
		return tagRepository.getTagsByBookId(id);
	}

	@GetMapping("/rename")
	public void rename(@RequestParam int source, @RequestParam String target) {
		Optional<Tag> optional = tagRepository.findById(source);
		if (optional.isPresent()) {
			Tag tag = optional.get();
			tag.setName(target);
			tagRepository.save(tag);
		}
	}

	@Transactional
	@GetMapping("/merge")
	public void merge(@RequestParam int source, @RequestParam int target) {
		Optional<Tag> tagSource = tagRepository.findById(source);
		Optional<Tag> tagTarget = tagRepository.findById(target);
		tagRepository.updateAllBookReferences(tagSource.get().getId(), tagTarget.get().getId());
		tagRepository.delete(tagSource.get());
	}

	@GetMapping("/image")
	public void image(@RequestParam int source, @RequestParam String image) {
		Optional<MyTag> optional = myTagRepository.findById(source);
		MyTag tag = null;
		if (optional.isPresent()) {
			tag = optional.get();
			tag.setImage(image);
		} else {
			tag = new MyTag(source, image);
		}
		myTagRepository.save(tag);
	}

	@GetMapping("/image/id")
	public Optional<MyTag> getImage(@RequestParam int source) {
		return myTagRepository.findById(source);
	}
}
