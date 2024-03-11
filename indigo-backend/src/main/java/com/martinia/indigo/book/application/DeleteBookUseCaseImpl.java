package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.DeleteBookUseCase;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.common.exceptions.MetadataRunningException;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class DeleteBookUseCaseImpl implements DeleteBookUseCase {

	@Resource
	private BookRepository bookRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Resource
	private MetadataSingleton metadataSingleton;

	@Override
	public void delete(String id) {

		if (metadataSingleton.isRunning()) {throw new MetadataRunningException();}

		bookRepository.findById(id).ifPresent(book -> bookRepository.delete(book));
	}

}
