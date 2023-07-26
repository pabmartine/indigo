package com.martinia.indigo.metadata.application.events;

import com.martinia.indigo.adapters.out.sqlite.service.CalibreRepository;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.commands.LoadBookCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.events.LoadBooksUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@Service
@Transactional
public class LoadBooksUseCaseImpl implements LoadBooksUseCase {

	private static final int BATCH_SIZE = 100;

	@Resource
	private CalibreRepository calibreRepository;

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Resource
	protected CommandBus commandBus;


	@Override
	public void start(boolean override) {

		log.info("Loading books ...");

		final Long numBooks = calibreRepository.count(null);

		metadataSingleton.setTotal(metadataSingleton.getTotal() + numBooks);

		int page = 0;

		while (page * BATCH_SIZE < numBooks) {

			final List<Book> books = calibreRepository.findAll(null, page, BATCH_SIZE, "id", "asc");

			for (Book book : books) {

				if (!metadataSingleton.isRunning()) {
					break;
				}

				commandBus.execute(LoadBookCommand.builder().bookId(book.getId()).override(override).build());

				log.debug("Indexed {}/{} books", metadataSingleton.getCurrent(), numBooks);

			}

			page++;
		}

	}

}
