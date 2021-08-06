package com.martinia.indigo.repository.indigo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.indigo.MyAuthor;

@Repository
public interface MyAuthorRepository extends CrudRepository<MyAuthor, Integer> {

	MyAuthor findByName(String name);
	
	MyAuthor findBySort(String sort);
	
	@Query("select f.author from FavoriteAuthor f where f.user = :user")
	List<Integer> getFavoriteAuthors(@Param("user") int user);

}
