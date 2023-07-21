package com.martinia.indigo.metadata.domain.ports.usecases.commands;

public interface FindAuthorMetadataUseCase {

	void find(String authorId, boolean override, long lastExecution, final String lang);
}
