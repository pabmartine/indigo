package com.martinia.indigo.book.domain.service;

import com.martinia.indigo.common.model.Search;

public interface CountAllBooksUseCase {

	Long count(Search search);

}
