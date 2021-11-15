package com.martinia.indigo.domain.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.domain.model.Tag;
import com.martinia.indigo.ports.in.rest.TagService;
import com.martinia.indigo.ports.out.mongo.TagRepository;

@Service
public class TagServiceImpl implements TagService {

	@Autowired
	TagRepository tagRepository;

	@Override
	public Tag findByName(String name) {
		return tagRepository.findByName(name);
	}

	@Override
	public void rename(String source, String target) {
		tagRepository.rename(source, target);

	}

	@Override
	public void merge(String source, String target) {
		tagRepository.merge(source, target);

	}

	@Override
	public void image(String source, String image) {
		tagRepository.image(source, image);

	}

	@Override
	public List<Tag> findAll(List<String> languages, String sort, String order) {
		return tagRepository.findAll(languages, sort, order);
	}

}
