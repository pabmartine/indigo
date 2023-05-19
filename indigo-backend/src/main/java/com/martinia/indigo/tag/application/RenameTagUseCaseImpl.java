package com.martinia.indigo.tag.application;

import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.repository.TagRepository;
import com.martinia.indigo.tag.domain.service.RenameTagUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RenameTagUseCaseImpl implements RenameTagUseCase {

	@Autowired
	TagRepository tagRepository;

	@Override
	public void rename(final String source, final String target) {
		tagRepository.rename(source, target);

	}

}
