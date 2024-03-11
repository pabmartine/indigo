package com.martinia.indigo.file.application.events;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.model.events.TagAddedEvent;
import com.martinia.indigo.file.domain.ports.usecases.events.SaveTagEpubFileEventUseCase;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@Slf4j
public class SaveTagEpubFileEventUseCaseImpl implements SaveTagEpubFileEventUseCase {

	@Resource
	private BookRepository bookRepository;
	@Resource
	private TagRepository tagRepository;
	@Resource
	private EventBus eventBus;

	@Resource
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;

	@Override
	@Transactional
	public synchronized void save(final String bookId) {

		bookRepository.findById(bookId).ifPresent(bookMongoEntity -> {
			final String bookLanguage = bookMongoEntity.getLanguages().get(0);
			bookMongoEntity.getTags().forEach(tag -> {
				final TagMongoEntity entity = tagRepository.findByName(tag).stream().findFirst().map(tagEntity -> {
					if (tagEntity.getNumBooks().getLanguages().get(bookLanguage) != null) {
						tagEntity.getNumBooks()
								.getLanguages()
								.put(bookLanguage, tagEntity.getNumBooks().getLanguages().get(bookLanguage) + 1);
					}
					else {
						tagEntity.getNumBooks().getLanguages().put(bookLanguage, 1);
					}
					tagEntity.getNumBooks().setTotal(tagEntity.getNumBooks().getTotal() + 1);
					return tagEntity;
				}).orElseGet(() -> {
					uploadEpubFilesSingleton.addTag();
					return new TagMongoEntity(tag, List.of(bookLanguage));
				});

				tagRepository.save(entity);

				eventBus.publish(TagAddedEvent.builder().tagId(entity.getId()).build());

			});

		});
	}

}


