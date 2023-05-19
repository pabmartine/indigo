package com.martinia.indigo.tag.domain.service;

import com.martinia.indigo.tag.domain.model.Tag;

import java.util.List;

public interface FindAllTagsUseCase {

	List<Tag> findAll(final List<String> languages, final String sort, final String order);

}
