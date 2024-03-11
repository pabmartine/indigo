package com.martinia.indigo.serie.domain.ports.usecases;

import java.util.List;

public interface FindNumSeriesUseCase {

	Long getNumSeries(final List<String> languages);

}
