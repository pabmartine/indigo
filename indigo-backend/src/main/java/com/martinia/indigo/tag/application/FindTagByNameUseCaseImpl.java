package com.martinia.indigo.tag.application;

import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.repository.TagRepository;
import com.martinia.indigo.tag.domain.service.FindTagByNameUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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
