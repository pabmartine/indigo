package com.martinia.indigo.metadata.domain.ports.adapters.wikipedia;

public interface FindWikipediaAuthorInfoPort {

	String[] getAuthorInfo(String subject, String lang);

}
