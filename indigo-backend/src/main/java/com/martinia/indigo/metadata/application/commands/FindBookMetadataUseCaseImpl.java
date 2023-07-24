package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsBookPort;
import com.martinia.indigo.metadata.domain.ports.adapters.google.FindGoogleBooksBookPort;
import com.martinia.indigo.metadata.domain.model.events.BookMetadataFoundEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindBookMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class FindBookMetadataUseCaseImpl implements FindBookMetadataUseCase {

	@Resource
	protected BookRepository bookRepository;

	@Resource
	protected ConfigurationRepository configurationRepository;

	@Resource
	private Optional<FindGoodReadsBookPort> findGoodReadsBookPort;

	@Resource
	private Optional<FindGoogleBooksBookPort> googleBooksComponent;

	@Resource
	private EventBus eventBus;

	@Override
	public void find(final String bookId, final boolean override, final long lastExecution) {

		bookRepository.findById(bookId).ifPresent(book -> {


			if (override || refreshBookMetadata(book)) {

				final Long pullTime = configurationRepository.findByKey("metadata.pull")
						.map(configuration -> Long.parseLong(configuration.getValue()))
						.orElse(null);

				final String goodreads = configurationRepository.findByKey("goodreads.key")
						.map(ConfigurationMongoEntity::getValue)
						.orElse(null);

				long milliseconds = (System.currentTimeMillis() - lastExecution);

				if (milliseconds < pullTime) {
					try {
						Thread.sleep(pullTime);
					}
					catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}

				String[] bookData = findGoodReadsBookPort.map(gr -> gr.findBook(goodreads, book.getTitle(), book.getAuthors(), false))
						.orElse(null);

				// Ratings && similar books
				String similar = null;
				if (bookData != null) {
					book.setRating(Float.valueOf(bookData[0]));
					book.setProvider(bookData[2]);

					if (StringUtils.isNotEmpty(bookData[1])) {
						similar = bookData[1];
					}

				}
				else {
					bookData = googleBooksComponent.map(gr -> gr.findBook(book.getTitle(), book.getAuthors())).orElse(null);

					if (bookData != null) {
						book.setRating(Float.valueOf(bookData[0]));
						book.setProvider(bookData[1]);
					}
				}

				book.setLastMetadataSync(Calendar.getInstance().getTime());
				bookRepository.save(book);

				log.info("Found metadata for {}", book.getTitle());


				eventBus.publish(BookMetadataFoundEvent.builder().bookId(book.getId()).similar(similar).build());

			}
		});
	}

	private boolean refreshBookMetadata(final BookMongoEntity book) {
		return (book.getRating() == 0 || book.getProvider() == null || CollectionUtils.isEmpty(book.getSimilar())) && (
				book.getLastMetadataSync() == null || book.getLastMetadataSync()
						.toInstant()
						.atZone(ZoneId.systemDefault())
						.toLocalDateTime()
						.plusDays(7)
						.isBefore(Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
	}
}
