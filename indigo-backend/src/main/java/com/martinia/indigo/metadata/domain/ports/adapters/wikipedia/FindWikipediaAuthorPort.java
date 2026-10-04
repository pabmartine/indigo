package com.martinia.indigo.metadata.domain.ports.adapters.wikipedia;

public interface FindWikipediaAuthorPort {

	String[] findAuthor(String subject, String lang, int cont);

	default String[] findAuthor(String subject, String lang, int cont, boolean descriptionNeeded) {
		return findAuthor(subject, lang, cont);
	}
}
