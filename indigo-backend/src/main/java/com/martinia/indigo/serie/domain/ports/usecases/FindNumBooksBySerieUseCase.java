package com.martinia.indigo.serie.domain.ports.usecases;

import java.util.List;
import java.util.Map;

public interface FindNumBooksBySerieUseCase {

	Map<String, Long> getNumBooksBySerie(final List<String> languages, final int page, final int size, final String sort, final String order);


}
