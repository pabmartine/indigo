package com.martinia.indigo.serie.domain.repository;

import com.martinia.indigo.domain.model.Book;

import java.util.List;
import java.util.Map;

public interface SerieRepository {

	List<Book> findBooksBySerie(String serie);

	Map<String, Long> getNumBooksBySerie(List<String> languages, int page, int size, String sort, String order);

	Long getNumSeries(List<String> languages);

}
