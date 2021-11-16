package com.martinia.indigo.ports.out.metadata;

import java.util.List;

import com.martinia.indigo.domain.model.Book;

public interface GoodReadsService {

	String[] findAuthor(String key, String subject);

	String[] findBook(String key, List<Book> list, String title, List<String> authors, boolean withAuthor);

}
