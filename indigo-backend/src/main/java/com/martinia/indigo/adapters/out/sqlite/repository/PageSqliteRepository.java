package com.martinia.indigo.adapters.out.sqlite.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.sqlite.entities.PagesSqliteEntity;

@Repository
public interface PageSqliteRepository extends CrudRepository<PagesSqliteEntity, Integer> {

	@Query("select p.value from PagesSqliteEntity p where p.book = :id")
	Optional<Integer> findPagesByBookId(int id);

}
