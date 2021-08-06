package com.martinia.indigo.repository.calibre;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.calibre.Author;

@Repository
public interface AuthorRepository extends CrudRepository<Author, Integer>, CustomAuthorRepository {

	Author findByName(String name);
	
	Author findBySort(String sort);
	
	@Query("from Author a, Book b, BooksAuthorsLink l where a.id = l.author and b.id = l.book and b.title = :title")
	List<Author> getAuthorsByBook(@Param("title") String title);
	
	@Query("select a.id, a.sort, count(l.book) as total from BooksAuthorsLink l, Author a where l.author = a.id and l.author = :id")
	Object[] getNumBooksByAuthorId(int id);

	

}
