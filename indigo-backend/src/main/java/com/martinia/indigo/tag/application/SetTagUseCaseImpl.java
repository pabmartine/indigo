package com.martinia.indigo.tag.application;

import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.repository.TagRepository;
import com.martinia.indigo.tag.domain.service.SetTagImageUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SetTagUseCaseImpl implements SetTagImageUseCase {

	@Autowired
	TagRepository tagRepository;

	@Override
	public void setImage(final String source, final String image) {
		tagRepository.setImage(source, image);

	}

}
