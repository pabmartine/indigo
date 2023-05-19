package com.martinia.indigo.tag.application;

import com.martinia.indigo.tag.domain.repository.TagRepository;
import com.martinia.indigo.tag.domain.service.MergeTagUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MergeTagUseCaseImpl implements MergeTagUseCase {

	@Autowired
	TagRepository tagRepository;

	@Override
	public void merge(final String source, final String target) {
		tagRepository.merge(source, target);

	}

}
