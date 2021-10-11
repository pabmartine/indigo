package com.martinia.indigo.repository.indigo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.indigo.FavoriteBook;

@Repository
public interface FavoriteBookRepository extends CrudRepository<FavoriteBook, Integer> {

	FavoriteBook findByBookAndUser(int book, int user);
	
	@Query("select f.book from FavoriteBook f where f.user = :user")
	List<Integer> findBooksByUser(@Param("user") int user);
	

}
