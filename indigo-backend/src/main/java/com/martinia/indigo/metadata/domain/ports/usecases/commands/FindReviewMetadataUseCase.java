package com.martinia.indigo.metadata.domain.ports.usecases.commands;

import com.martinia.indigo.metadata.domain.model.MetadataItemResult;

public interface FindReviewMetadataUseCase {

	MetadataItemResult find(String bookId, boolean override, final String lang);
}
