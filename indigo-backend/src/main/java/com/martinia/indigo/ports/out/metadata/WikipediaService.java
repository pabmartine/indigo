package com.martinia.indigo.ports.out.metadata;

public interface WikipediaService {

	String[] getAuthorInfo(String subject, String lang);

	String[] findAuthor(String subject, String lang);

}
