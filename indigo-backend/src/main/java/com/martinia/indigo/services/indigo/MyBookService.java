package com.martinia.indigo.services.indigo;

import java.util.List;

import com.martinia.indigo.model.calibre.Book;
import com.martinia.indigo.model.indigo.MyBook;

public interface MyBookService {

	List<Integer> getSentBooks(int user);

	List<Book> getFavoriteBooks(int user);

	MyBook getBookInfoBy(int id, boolean local);

}
