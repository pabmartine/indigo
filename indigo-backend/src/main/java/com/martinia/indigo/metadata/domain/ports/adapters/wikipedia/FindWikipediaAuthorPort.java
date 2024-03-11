package com.martinia.indigo.metadata.domain.ports.adapters.wikipedia;

public interface FindWikipediaAuthorPort {

	String[] findAuthor(String subject, String lang, int cont);

}
