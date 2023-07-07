package com.martinia.indigo.metadata.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.metadata.application.common.BaseMetadataUseCaseImpl;
import com.martinia.indigo.metadata.domain.service.RefreshBookMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class RefreshBookMetadataUseCaseImpl extends BaseMetadataUseCaseImpl implements RefreshBookMetadataUseCase {

	@Override
	public Optional<Book> findBookMetadata(String path) {

		if (goodreads == null) {
			goodreads = configurationRepository.findByKey("goodreads.key").get().getValue();
		}

		return bookRepository.findByPath(path).map(book -> {
			Book _book = findBookMetadata(true, book);
			bookRepository.save(_book);
			return Optional.of(_book);
		}).orElse(Optional.empty());

	}

}
