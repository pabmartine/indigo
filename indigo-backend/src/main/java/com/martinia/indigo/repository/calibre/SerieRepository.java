package com.martinia.indigo.repository.calibre;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.calibre.Author;

@Repository
public interface SerieRepository extends CrudRepository<Author, Integer>, CustomSerieRepository {

	@Query("select s.name from Serie s, BooksSeriesLink l where l.series = s.id and l.book = :id")
	String getSerieByBook(@Param("id") int id);

	@Query("select b.path from Book b, BooksSeriesLink l where l.book = b.id and l.series = :id")
	List<String> getBookPathSerieById(@Param("id") int id, Pageable pageable);

}
