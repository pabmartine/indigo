package com.martinia.indigo.metadata.domain.ports.usecases.wikipedia;

public interface FindWikipediaAuthorUseCase {

	String[] findAuthor(String subject, String lang, int cont);

	default String[] findAuthor(String subject, String lang, int cont, boolean descriptionNeeded) {
		return findAuthor(subject, lang, cont);
	}
}
