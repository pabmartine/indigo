package com.martinia.indigo.tag.application;

import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.MergeTagUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MergeTagUseCaseImpl implements MergeTagUseCase {

	@Resource
	private TagRepository tagRepository;

	@Override
	public void merge(final String source, final String target) {
		tagRepository.merge(source, target);

	}

}
