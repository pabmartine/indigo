package com.martinia.indigo.metadata.domain.ports.usecases.commands;

public interface FindBookMetadataUseCase {

	void find(String bookId, boolean override, long lastExecution);
}
