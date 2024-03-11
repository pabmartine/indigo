package com.martinia.indigo.metadata.domain.ports.usecases.wikipedia;

public interface FindWikipediaAuthorUseCase {

	String[] findAuthor(String subject, String lang, int cont);

}
