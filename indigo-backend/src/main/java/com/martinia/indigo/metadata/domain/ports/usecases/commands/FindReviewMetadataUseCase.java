package com.martinia.indigo.metadata.domain.ports.usecases.commands;

public interface FindReviewMetadataUseCase {

	void find(String bookId, boolean override, long lastExecution, final String lang);
}
