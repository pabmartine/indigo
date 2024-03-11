package com.martinia.indigo.tag.application;

import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.SetTagImageUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class SetTagUseCaseImpl implements SetTagImageUseCase {

	@Resource
	private TagRepository tagRepository;

	@Override
	public void setImage(final String source, final String image) {
		tagRepository.findById(source).ifPresent(tag -> {
			tag.setImage(image);
			tagRepository.save(tag);
		});
	}

}
