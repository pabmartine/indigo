package com.martinia.indigo.metadata.application.events;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.metadata.domain.ports.usecases.events.UpdateImageTagMetadataUseCase;
import com.martinia.indigo.tag.domain.ports.usecases.UpdateImageTagUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Slf4j
@Service
@Transactional
public class UpdateImageTagMetadataUseCaseImpl implements UpdateImageTagMetadataUseCase {

	@Resource
	private BookRepository bookRepository;

	@Resource
	private UpdateImageTagUseCase updateImageTagUseCase;

	@Override
	@Transactional
	public void updateImage(final String bookId) {
		bookRepository.findById(bookId).ifPresent(book -> {
			book.getTags().forEach(tag -> {
				updateImageTagUseCase.updateImage(tag);
			});
		});
	}

}
