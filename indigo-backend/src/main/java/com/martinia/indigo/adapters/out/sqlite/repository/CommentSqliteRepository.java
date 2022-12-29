package com.martinia.indigo.adapters.out.sqlite.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.sqlite.entities.CommentsSqliteEntity;

import java.util.Optional;

@Repository
public interface CommentSqliteRepository extends CrudRepository<CommentsSqliteEntity, Integer> {

	@Query("select c.text from CommentsSqliteEntity c where c.book = :id")
	Optional<String> findTextByBookId(int id);

}
