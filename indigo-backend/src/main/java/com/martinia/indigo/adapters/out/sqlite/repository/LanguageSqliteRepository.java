package com.martinia.indigo.adapters.out.sqlite.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.adapters.out.sqlite.entities.LanguageSqliteEntity;

@Repository
public interface LanguageSqliteRepository extends CrudRepository<LanguageSqliteEntity, Integer> {

	@Query("select t.langCode from LanguageSqliteEntity t, BooksLanguagesLinkSqliteEntity l where t.id = l.langCode and l.book = :id")
	List<String> getLanguageByBookId(int id);

}
