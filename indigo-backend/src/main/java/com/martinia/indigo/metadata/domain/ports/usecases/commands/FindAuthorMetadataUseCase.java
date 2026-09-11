package com.martinia.indigo.metadata.domain.ports.usecases.commands;

import com.martinia.indigo.metadata.domain.model.MetadataItemResult;

public interface FindAuthorMetadataUseCase {

	MetadataItemResult find(String authorId, boolean override, long lastExecution, final String lang);
}
