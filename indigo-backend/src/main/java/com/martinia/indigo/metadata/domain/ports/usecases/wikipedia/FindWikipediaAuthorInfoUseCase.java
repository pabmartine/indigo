package com.martinia.indigo.metadata.domain.ports.usecases.wikipedia;

public interface FindWikipediaAuthorInfoUseCase {

	String[] getAuthorInfo(String subject, String lang);

	default String[] getAuthorInfo(String subject, String lang, boolean descriptionNeeded) {
		return getAuthorInfo(subject, lang);
	}
}
