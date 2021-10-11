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
	public void updateAllBookReferences(int sourceId, int targetId) {
		tagRepository.updateAllBookReferences(sourceId, targetId);

	}

	@Override
	public List<String> getTagsByBookId(int id) {
		return tagRepository.getTagsByBookId(id);
	}

	@Override
	public Tag getTagByName(String name) {
		return tagRepository.findTagByName(name);
	}

	public void delete(Tag tag) {
		tagRepository.delete(tag);
	}

	public Optional<Tag> findById(int target) {
		return tagRepository.findById(target);
	}

	public void save(Tag tag) {
		tagRepository.save(tag);
	}

	public long count() {
		return tagRepository.count();
	}

	public List<Object[]> getNumBooksByTag(String sort, String order) {
		return tagRepository.getNumBooksByTag(sort, order);
	}

}
