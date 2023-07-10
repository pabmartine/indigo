package com.martinia.indigo.tag.application;

import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.FindTagByNameUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class FindTagByNameUseCaseImpl implements FindTagByNameUseCase {

	@Resource
	private TagRepository tagRepository;

	@Override
	public Optional<Tag> findByName(final String name) {
		return tagRepository.findByName(name);
	}

}