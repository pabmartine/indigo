package com.martinia.indigo.services.calibre;

import java.util.List;

import com.martinia.indigo.model.calibre.Author;

public interface AuthorService {

	Author findBySort(String author);

	Object[] getNumBooksByAuthorId(Integer id);

	long count();

	List<Object[]> getNumBooksByAuthor(int page, int size, String sort, String order);

}
