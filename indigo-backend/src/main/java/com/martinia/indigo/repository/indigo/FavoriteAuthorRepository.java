package com.martinia.indigo.repository.indigo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.indigo.FavoriteAuthor;

@Repository
public interface FavoriteAuthorRepository extends CrudRepository<FavoriteAuthor, Integer> {

	FavoriteAuthor findByAuthorAndUser(int author, int user);

}
