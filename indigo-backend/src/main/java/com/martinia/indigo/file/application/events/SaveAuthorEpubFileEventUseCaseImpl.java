package com.martinia.indigo.file.application.events;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.model.events.AuthorAddedEvent;
import com.martinia.indigo.file.domain.ports.usecases.events.SaveAuthorEpubFileEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class SaveAuthorEpubFileEventUseCaseImpl implements SaveAuthorEpubFileEventUseCase {

	@Resource
	private BookRepository bookRepository;
	@Resource
	private AuthorRepository authorRepository;
	@Resource
	private EventBus eventBus;

	@Resource
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;

	@Override
	@Transactional
	public synchronized void save(final String bookId, final String authorImage) {

		bookRepository.findById(bookId).ifPresent(bookMongoEntity -> {

			bookMongoEntity.getAuthors().forEach(author -> {

				if (author.equalsIgnoreCase("VV., AA.")) {
					author = "AA. VV.";
				}

				final String finalAuthor = author;
				final AuthorMongoEntity entity = authorRepository.findByName(author).stream().findFirst().map(authorMongoEntity -> {
					authorMongoEntity.setImage(authorImage);
					authorMongoEntity.getNumBooks().setTotal(authorMongoEntity.getNumBooks().getTotal() + 1);
					authorMongoEntity.getNumBooks().getLanguages().keySet().forEach(key -> {
						if (authorMongoEntity.getNumBooks().getLanguages().get(key) != null) {
							authorMongoEntity.getNumBooks()
									.getLanguages()
									.put(key, authorMongoEntity.getNumBooks().getLanguages().get(key) + 1);
						}
						else {
							authorMongoEntity.getNumBooks().getLanguages().put(key, 1);
						}
					});
					return authorMongoEntity;
				}).orElseGet(() -> {
					uploadEpubFilesSingleton.addAuthor();
					final Map<String, Integer> languages = new HashMap<>();
					languages.put(bookMongoEntity.getLanguages().get(0), 1);
					return AuthorMongoEntity.builder()
							.name(finalAuthor)
							.image(authorImage)
							.numBooks(NumBooksMongo.builder().total(1).languages(languages).build())
							.build();
				});

				authorRepository.save(entity);

				eventBus.publish(AuthorAddedEvent.builder().authorId(entity.getId()).build());
			});

		});
	}

}


