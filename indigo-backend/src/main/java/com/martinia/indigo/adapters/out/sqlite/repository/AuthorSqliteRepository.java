package com.martinia.indigo.adapters.out.sqlite.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.sqlite.entities.AuthorSqliteEntity;

@Repository
public interface AuthorSqliteRepository extends CrudRepository<AuthorSqliteEntity, Integer> {

	List<AuthorSqliteEntity> findBySort(String sort);

}
