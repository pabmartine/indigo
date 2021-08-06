package com.martinia.indigo.repository.calibre;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface CustomTagRepository {

	List<Object[]> getNumBooksByTag(String sort, String order);

}
