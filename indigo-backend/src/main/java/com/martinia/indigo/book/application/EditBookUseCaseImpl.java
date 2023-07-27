package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.EditBookUseCase;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class EditBookUseCaseImpl implements EditBookUseCase {

	@Resource
	private BookRepository bookRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	public void edit(Book book) {
		bookRepository.findById(book.getId()).ifPresent(entity -> bookRepository.save(bookMongoMapper.domain2Entity(book)));
	}

}
