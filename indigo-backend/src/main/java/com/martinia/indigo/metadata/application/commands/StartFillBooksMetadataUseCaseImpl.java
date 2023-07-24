package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.ports.commands.FindBookMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartFillBooksMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@Service
@Transactional
public class StartFillBooksMetadataUseCaseImpl implements StartFillBooksMetadataUseCase {

	private static final int BATCH_SIZE = 100;

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Resource
	protected BookRepository bookRepository;

	@Resource
	protected CommandBus commandBus;

	@Override
	@Transactional
	public void start(boolean override) {

		log.info("Finding metadata for all book library");

		metadataSingleton.setMessage("obtaining_metadata_books");

		Long numBooks = bookRepository.count();

		metadataSingleton.setTotal(metadataSingleton.getTotal() + numBooks);

		long lastExecution = 0;

		int page = 0;
		int size = BATCH_SIZE;
		while (page * size < numBooks) {

			if (!metadataSingleton.isRunning()) {
				break;
			}

			List<BookMongoEntity> books = bookRepository.findAll(null, page, size, "id", "asc");

			if (!CollectionUtils.isEmpty(books)) {
				for (BookMongoEntity book : books) {

					if (!metadataSingleton.isRunning()) {
						break;
					}

					metadataSingleton.setCurrent(metadataSingleton.getCurrent() + 1);

					commandBus.executeAndWait(
							FindBookMetadataCommand.builder().bookId(book.getId()).override(override).lastExecution(lastExecution).build());
					lastExecution = System.currentTimeMillis();

					log.debug("Obtained {}/{} books metadata", metadataSingleton.getCurrent(), numBooks);
				}
			}

			page++;

		}

	}

}
