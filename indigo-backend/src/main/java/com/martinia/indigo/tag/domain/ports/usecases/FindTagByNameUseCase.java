package com.martinia.indigo.tag.domain.ports.usecases;

import com.martinia.indigo.tag.domain.model.Tag;

import java.util.Optional;

public interface FindTagByNameUseCase {

	Optional<Tag> findByName(final String name);

}
