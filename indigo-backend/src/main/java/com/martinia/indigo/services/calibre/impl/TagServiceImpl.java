package com.martinia.indigo.services.calibre.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.calibre.Tag;
import com.martinia.indigo.repository.calibre.TagRepository;
import com.martinia.indigo.services.calibre.TagService;

@Service
public class TagServiceImpl implements TagService {

	@Autowired
	TagRepository tagRepository;

	@Override
	public Tag findByName(String name) {
		return tagRepository.findByName(name);
	}

	@Override
	public List<String> getTagsByBookId(int id) {
		return tagRepository.getTagsByBookId(id);
	}

	@Override
	public Tag getTagByName(String name) {
		return tagRepository.findTagByName(name);
	}

	public long count() {
		return tagRepository.count();
	}

	public List<Object[]> getNumBooksByTag(String sort, String order) {
		return tagRepository.getNumBooksByTag(sort, order);
	}

	@Override
	public void rename(int source, String target) {
		Optional<Tag> optional = tagRepository.findById(source);
		if (optional.isPresent()) {
			Tag tag = optional.get();
			tag.setName(target);
			tagRepository.save(tag);
		}
	}

	@Override
	public void merge(int source, int target) {
		Optional<Tag> tagSource = tagRepository.findById(source);
		Optional<Tag> tagTarget = tagRepository.findById(target);
		tagRepository.updateAllBookReferences(tagSource.get()
				.getId(),
				tagTarget.get()
						.getId());
		tagRepository.delete(tagSource.get());
	}

}
