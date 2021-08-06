package com.martinia.indigo.repository.indigo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.indigo.FavoriteBook;

@Repository
public interface FavoriteBookRepository extends CrudRepository<FavoriteBook, Integer> {

	@Query("from FavoriteBook f where f.user = :user and f.book = :book")
	FavoriteBook getFavoriteBook(@Param("book") int book, @Param("user") int user);

}
