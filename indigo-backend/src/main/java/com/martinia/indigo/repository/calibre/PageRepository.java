package com.martinia.indigo.repository.calibre;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.calibre.Pages;

@Repository
public interface PageRepository extends CrudRepository<Pages, Integer> {

	@Query("select p.value from Pages p where p.book = :id")
	int findPagesByBookId(int id);

}
