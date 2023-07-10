package com.martinia.indigo.tag.application;

import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.FindAllTagsUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FindAllTagsUseCaseImpl implements FindAllTagsUseCase {

	@Resource
	private TagRepository tagRepository;

	@Override
	public List<Tag> findAll(final List<String> languages, final String sort, final String order) {
		return tagRepository.findAll(languages, sort, order);
	}

}
