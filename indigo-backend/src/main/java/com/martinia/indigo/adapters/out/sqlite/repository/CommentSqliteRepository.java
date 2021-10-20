package com.martinia.indigo.adapters.out.sqlite.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.sqlite.entities.CommentsSqliteEntity;

@Repository
public interface CommentSqliteRepository extends CrudRepository<CommentsSqliteEntity, Integer> {

	@Query("select c.text from CommentsSqliteEntity c where c.book = :id")
	String findTextByBookId(int id);

}
