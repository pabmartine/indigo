package com.martinia.indigo.metadata.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.metadata.application.common.BaseMetadataUseCaseImpl;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshBookMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Slf4j
@Service
public class RefreshBookMetadataUseCaseImpl extends BaseMetadataUseCaseImpl implements RefreshBookMetadataUseCase {

	@Resource
	private BookMongoMapper mapper;

	@Override
	public Optional<Book> findBookMetadata(String path) {

		if (Optional.ofNullable(goodreads).isEmpty()) {
			goodreads = configurationRepository.findByKey("goodreads.key").map(ConfigurationMongoEntity::getValue).orElse(null);
		}

		return bookRepository.findByPath(path).map(book -> {
			BookMongoEntity _book = findBookMetadata(true, book);
			bookRepository.save(_book);
			return Optional.of(mapper.entity2Domain(_book));
		}).orElse(Optional.empty());

	}

}
