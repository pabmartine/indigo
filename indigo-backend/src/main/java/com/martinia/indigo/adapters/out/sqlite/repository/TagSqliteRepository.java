package com.martinia.indigo.adapters.out.sqlite.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.sqlite.entities.TagSqliteEntity;

@Repository
public interface TagSqliteRepository extends CrudRepository<TagSqliteEntity, Integer> {

	@Query("select t.name from TagSqliteEntity t, BooksTagsLinkSqliteEntity l where t.id = l.tag and l.book = :id")
	List<String> getTagsByBookId(int id);

}
