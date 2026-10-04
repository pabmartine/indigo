package com.martinia.indigo.metadata.domain.ports.adapters.wikipedia;

public interface FindWikipediaAuthorInfoPort {

	String[] getAuthorInfo(String subject, String lang);

	default String[] getAuthorInfo(String subject, String lang, boolean descriptionNeeded) {
		return getAuthorInfo(subject, lang);
	}
}
