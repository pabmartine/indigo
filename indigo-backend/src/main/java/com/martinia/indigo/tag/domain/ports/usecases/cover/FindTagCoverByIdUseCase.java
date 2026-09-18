package com.martinia.indigo.tag.domain.ports.usecases.cover;

import com.martinia.indigo.tag.domain.model.TagCoverResult;

public interface FindTagCoverByIdUseCase {

	TagCoverResult getCover(String id);

}
