package com.martinia.indigo.tag.domain.service;

import com.martinia.indigo.tag.domain.model.Tag;

import java.util.List;
import java.util.Optional;

public interface FindTagByNameUseCase {

	Optional<Tag> findByName(final String name);

}
