package com.martinia.indigo.services.indigo;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.model.indigo.MyBook;

public interface MyBookService {

	List<Integer> getSentBooks(int user);

	void save(MyBook myBook);

	Optional<MyBook> findById(int id);

	List<Integer> getFavoriteBooks(int user);

}
