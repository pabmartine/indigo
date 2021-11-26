package com.martinia.indigo.ports.out.metadata;

import java.util.List;

public interface GoodReadsService {

	String[] findAuthor(String key, String subject);

	String[] findBook(String key, String title, List<String> authors, boolean withAuthor);

}
