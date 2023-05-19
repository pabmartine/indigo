package com.martinia.indigo.tag.domain.service;

import com.martinia.indigo.tag.domain.model.Tag;

import java.util.List;

public interface MergeTagUseCase {

	void merge(final String source, final String target);

}
