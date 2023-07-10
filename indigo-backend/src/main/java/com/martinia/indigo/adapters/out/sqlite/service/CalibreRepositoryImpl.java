package com.martinia.indigo.adapters.out.sqlite.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.martinia.indigo.adapters.out.sqlite.entities.AuthorSqliteEntity;
import com.martinia.indigo.adapters.out.sqlite.entities.BookSqliteEntity;
import com.martinia.indigo.adapters.out.sqlite.mapper.AuthorSqliteMapper;
import com.martinia.indigo.adapters.out.sqlite.mapper.BookSqliteMapper;
import com.martinia.indigo.adapters.out.sqlite.repository.AuthorSqliteRepository;
import com.martinia.indigo.adapters.out.sqlite.repository.BookSqliteRepository;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.common.model.Search;

@Component
public class CalibreRepositoryImpl implements CalibreRepository {

	@Autowired
	BookSqliteRepository bookSqliteRepository;

	@Autowired
	AuthorSqliteRepository authorSqliteRepository;

	@Autowired
	BookSqliteMapper bookSqliteMapper;

	@Autowired
	AuthorSqliteMapper authorSqliteMapper;

	@Override
	public Long count(Search search) {
		return bookSqliteRepository.count();
	}

	@Override
	public List<Book> findAll(Search search, int page, int size, String sort, String order) {
		List<BookSqliteEntity> entities = bookSqliteRepository
				.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sort)))
				.getContent();
		return bookSqliteMapper.entities2Domains(entities);
	}

	@Override
	public List<Author> findAuthorsByBook(String book) {
		List<AuthorSqliteEntity> entities = authorSqliteRepository.findAuthors(Integer.parseInt(book));
		return authorSqliteMapper.entities2Domains(entities);
	}



}
