package com.martinia.indigo.repository.calibre;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface CustomAuthorRepository {

	List<Object[]> getNumBooksByAuthor(int page, int size, String sort, String order);

}
