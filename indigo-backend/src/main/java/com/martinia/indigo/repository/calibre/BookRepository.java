package com.martinia.indigo.repository.calibre;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.calibre.Book;

@Repository
public interface BookRepository extends PagingAndSortingRepository<Book, Integer>, CustomBookRepository {

	@Query("select b from Book b, Author a, BooksAuthorsLink l where a.id = l.author and b.id = l.book and a.sort = :name")
	List<Book> getBooksByAuthor(@Param("name") String name, Pageable pageable);

	@Query("select b from Book b, BooksTagsLink l, Tag t where b.id = l.book and t.id = l.tag and t.name = :name")
	List<Book> getBooksByTag(@Param("name") String name, Pageable pageable);

	@Query("select b from Book b, Serie s, BooksSeriesLink l where s.id = l.series and b.id = l.book and s.name = :name")
	List<Book> getBooksBySerie(@Param("name") String name, Pageable pageable);

	@Query("select count(*) from BooksTagsLink l, Tag s where l.tag = s.id and s.name = :name")
	long countBooksByTag(@Param("name") String name);

	@Query("select count(*) from BooksAuthorsLink l, Author s where l.author = s.id and s.sort = :name")
	long countBooksByAuthor(@Param("name") String name);

	@Query("select count(*) from BooksSeriesLink l, Serie s where l.series = s.id and s.name = :name")
	long countBooksBySerie(@Param("name") String name);
	
	
	@Query("select b from Book b where id in ( "+ 
			"select l.book from BooksTagsLink l " + 
			"where l.tag in (select t.tag from BooksTagsLink t where t.book = :id ) " + 
			"and l.book not in (:id) " + 
			"group by l.book having count(l.id) = (select count(t.tag) from BooksTagsLink t where t.book = :id)" + 
			") " + 
			"and (b.pubdate > DATE((select t.pubdate from Book t where t.id = :id), '-5 year') " + 
			"or b.pubdate > DATE((select t.pubdate from Book t where t.id = :id), '+5 year')) " +
			"order by random()")
	List<Book> getBookRecommendations(@Param("id") int id, Pageable pageable);

	List<Book> findByTitle(String title);
	
	
}
