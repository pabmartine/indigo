package com.martinia.indigo.adapters.out.sqlite.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.sqlite.entities.AuthorSqliteEntity;

import java.util.Optional;

@Repository
public interface SerieSqliteRepository extends CrudRepository<AuthorSqliteEntity, Integer> {

	@Query("select s.name from SerieSqliteEntity s, BooksSeriesLinkSqliteEntity l where l.series = s.id and l.book = :id")
	Optional<String> getSerieByBook(@Param("id") int id);

}
