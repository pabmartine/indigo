package com.martinia.indigo.book.domain.service;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.domain.model.Search;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

public interface FindBookByPathUseCase {

	Optional<Book> findByPath(String path);

}
