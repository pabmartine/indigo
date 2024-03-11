package com.martinia.indigo.metadata.application;

import com.martinia.indigo.adapters.out.sqlite.service.CalibreRepository;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.util.UtilComponent;
import com.martinia.indigo.metadata.domain.model.commands.FindBookMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.FindReviewMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshBookMetadataUseCase;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
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

	@Resource
	protected CalibreRepository calibreRepository;

	@Resource
	private UtilComponent utilComponent;

	@Resource
	protected TagRepository tagRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	public Optional<Book> findBookMetadata(String path, String lang) {

		return bookRepository.findByPath(path).map(book -> {

			parseLocalBook(book);

			commandBus.executeAndWait(FindBookMetadataCommand.builder().bookId(book.getId()).override(true).lastExecution(0).build());
			commandBus.executeAndWait(FindReviewMetadataCommand.builder().bookId(book.getId()).override(true).lastExecution(0).lang(lang).build());
			return Optional.of(mapper.entity2Domain(bookRepository.findByPath(path).get()));
		}).orElse(Optional.empty());

	}

	private void parseLocalBook(final BookMongoEntity entity) {
		final Book book = calibreRepository.findBookByPath(entity.getPath());
		entity.setImage(utilComponent.getBase64Cover(book.getPath(), true));
		entity.setComment(book.getComment());
		entity.setPages(book.getPages());
		entity.setPubDate(book.getPubDate());
		//TODO: authors, tags and languages not done becouse of the control of its own tables and values
		bookRepository.save(entity);
	}

}
