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

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
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
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.file.application.PendingImportService pendingImports;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.metadata.application.MetadataActivityService activity;

	@Override
	@Transactional
	public synchronized void save(final String bookId, final String authorImage) {
		save(bookId, authorImage, true);
	}

	@Override
	@Transactional
	public synchronized void save(final String bookId, final String authorImage, final boolean newBook) {
		if (pendingImports != null && pendingImports.done(bookId, "authorsDone")) return;
		if (!newBook) {
			return;
		}

		bookRepository.findById(bookId).ifPresentOrElse(bookMongoEntity -> {

			bookMongoEntity.getAuthors().forEach(author -> {

				if (author.equalsIgnoreCase("VV., AA.")) {
					author = "AA. VV.";
				}

				final String finalAuthor = author;
				final AuthorMongoEntity entity = authorRepository.findByName(author).stream().findFirst().map(authorMongoEntity -> {
					if (authorImage != null && !authorImage.isBlank()
							&& (activity == null || !activity.isLocked("AUTHORS", authorMongoEntity.getId()))) {
						authorMongoEntity.setImage(authorImage);
						var sources = new HashMap<String, String>(java.util.Optional.ofNullable(authorMongoEntity.getMetadataSources()).orElseGet(Map::of));
						sources.put("image", "EPUB");
						authorMongoEntity.setMetadataSources(sources);
					}
					authorMongoEntity.getNumBooks().setTotal(authorMongoEntity.getNumBooks().getTotal() + 1);

					bookMongoEntity.getLanguages().forEach(bookLanguage -> {
						Integer current = authorMongoEntity.getNumBooks().getLanguages().get(bookLanguage);
						authorMongoEntity.getNumBooks().getLanguages().put(bookLanguage, current == null ? 1 : current + 1);
					});

					return authorMongoEntity;
				}).orElseGet(() -> {
					uploadEpubFilesSingleton.addAuthor();
					final Map<String, Integer> languages = new HashMap<>();
					languages.put(bookMongoEntity.getLanguages().get(0), 1);
					return AuthorMongoEntity.builder()
							.name(finalAuthor)
							.image(authorImage)
							.metadataSources(authorImage == null || authorImage.isBlank() ? Map.of() : Map.of("image", "EPUB"))
							.numBooks(NumBooksMongo.builder().total(1).languages(languages).build())
							.build();
				});

				authorRepository.save(entity);

				eventBus.publish(AuthorAddedEvent.builder().authorId(entity.getId()).build());
			});

			if (pendingImports != null) pendingImports.complete(bookId, "authorsDone");
		}, () -> {
			throw new IllegalStateException("Book " + bookId + " is not visible yet while creating its authors");
		});
	}

}
