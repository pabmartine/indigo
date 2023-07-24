package com.martinia.indigo.metadata.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.domain.ports.commands.FindBookMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshBookMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class RefreshBookMetadataUseCaseImpl implements RefreshBookMetadataUseCase {

	@Resource
	private BookRepository bookRepository;

	@Resource
	private BookMongoMapper mapper;

	@Resource
	private CommandBus commandBus;

	@Override
	public Optional<Book> findBookMetadata(String path) {

		return bookRepository.findByPath(path).map(book -> {
			commandBus.executeAndWait(FindBookMetadataCommand.builder().bookId(book.getId()).override(true).lastExecution(0).build());
			return Optional.of(mapper.entity2Domain(bookRepository.findByPath(path).get()));
		}).orElse(Optional.empty());

	}

}
