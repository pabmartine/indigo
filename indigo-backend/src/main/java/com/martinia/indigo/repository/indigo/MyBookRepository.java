package com.martinia.indigo.repository.indigo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.indigo.MyBook;

@Repository
public interface MyBookRepository extends CrudRepository<MyBook, Integer> {

	@Query("select f.book from FavoriteBook f where f.user = :user")
	List<Integer> getFavoriteBooks(@Param("user") int user);
	
	@Query("select distinct n.book from Notification n where n.user = :user")
	List<Integer> getSentBooks(@Param("user") int user);

}
