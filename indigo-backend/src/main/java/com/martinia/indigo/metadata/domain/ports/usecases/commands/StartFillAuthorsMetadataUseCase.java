package com.martinia.indigo.metadata.domain.ports.usecases.commands;

public interface StartFillAuthorsMetadataUseCase {

	default void start(boolean override, final String lang) {
		start(override, lang, 0);
	}

	void start(boolean override, String lang, long runId);
}
