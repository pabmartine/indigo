package com.martinia.indigo.metadata.domain.ports.usecases.commands;

public interface StartFillReviewsMetadataUseCase {

	void start(boolean override, final String lang);
}
