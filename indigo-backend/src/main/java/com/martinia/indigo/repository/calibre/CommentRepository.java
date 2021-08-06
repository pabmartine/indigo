package com.martinia.indigo.repository.calibre;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.calibre.Comments;

@Repository
public interface CommentRepository extends CrudRepository<Comments	, Integer> {

	@Query("select c.text from Comments c where c.book = :id")
	String findTextByBookId(int id);

}
