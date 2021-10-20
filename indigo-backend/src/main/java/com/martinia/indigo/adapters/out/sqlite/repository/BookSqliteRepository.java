package com.martinia.indigo.adapters.out.sqlite.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.sqlite.entities.BookSqliteEntity;

@Repository
public interface BookSqliteRepository extends PagingAndSortingRepository<BookSqliteEntity, Integer> {

}
