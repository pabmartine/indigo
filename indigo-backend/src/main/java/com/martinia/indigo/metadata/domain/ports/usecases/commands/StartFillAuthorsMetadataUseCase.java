package com.martinia.indigo.metadata.domain.ports.usecases.commands;

public interface StartFillAuthorsMetadataUseCase {

	void start(boolean override, final String lang);
}
