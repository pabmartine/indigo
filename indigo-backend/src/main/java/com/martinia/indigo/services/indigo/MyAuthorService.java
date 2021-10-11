package com.martinia.indigo.services.indigo;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.model.indigo.MyAuthor;

public interface MyAuthorService {

	List<Integer> getFavoriteAuthors(int user);

	Optional<MyAuthor> findById(int id);

	void save(MyAuthor myAuthor);

	MyAuthor findBySort(String author);

}
