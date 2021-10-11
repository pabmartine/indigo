package com.martinia.indigo.services.calibre;

import java.util.List;

import org.springframework.data.domain.Pageable;

public interface SerieService {

	String getSerieByBook(int id);

	List<String> getBookPathSerieById(int id, Pageable pageable);

	long count();

	List<Object[]> getNumBooksBySerie(int page, int size, String sort, String order);

}
