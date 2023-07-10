package com.martinia.indigo.book.domain.ports.usecases;

import com.martinia.indigo.common.model.Search;

public interface CountAllBooksUseCase {

	Long count(Search search);

}
