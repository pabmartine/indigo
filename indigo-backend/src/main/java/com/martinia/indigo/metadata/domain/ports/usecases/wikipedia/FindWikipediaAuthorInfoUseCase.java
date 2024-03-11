package com.martinia.indigo.metadata.domain.ports.usecases.wikipedia;

public interface FindWikipediaAuthorInfoUseCase {

	String[] getAuthorInfo(String subject, String lang);

}
