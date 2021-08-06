package com.martinia.indigo.repository.indigo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.indigo.FavoriteAuthor;

@Repository
public interface FavoriteAuthorRepository extends CrudRepository<FavoriteAuthor, Integer> {

	@Query("from FavoriteAuthor f where f.user = :user and f.author = :author")
	FavoriteAuthor getFavoriteAuthor(@Param("author") int author, @Param("user") int user);

}
