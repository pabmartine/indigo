package com.martinia.indigo.tag.application;

import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.RenameTagUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RenameTagUseCaseImpl implements RenameTagUseCase {

	@Resource
	private TagRepository tagRepository;

	@Override
	public void rename(final String source, final String target) {
		tagRepository.rename(source, target);

	}

}
