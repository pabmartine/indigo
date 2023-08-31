package com.martinia.indigo.adapters.out.sqlite.repository;

import com.martinia.indigo.book.domain.model.Book;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.sqlite.entities.BookSqliteEntity;

import java.util.Optional;

@Repository
public interface BookSqliteRepository extends PagingAndSortingRepository<BookSqliteEntity, Integer> {

	Optional<BookSqliteEntity> findByPath(final String path);

}
