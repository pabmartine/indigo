package com.martinia.indigo.adapters.out.sqlite.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.sqlite.entities.AuthorSqliteEntity;

@Repository
public interface AuthorSqliteRepository extends CrudRepository<AuthorSqliteEntity, Integer> {

	
	List<AuthorSqliteEntity> findBySort(String sort);
	
	List<AuthorSqliteEntity> findByName(String name);
	
	@Query("select a from AuthorSqliteEntity a, BooksAuthorsLinkSqliteEntity l where a.id = l.author and l.book = :book")
	List<AuthorSqliteEntity> findAuthors(@Param("book") int book);

}
