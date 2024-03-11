package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.adapters.out.sqlite.service.CalibreRepository;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.common.util.UtilComponent;
import com.martinia.indigo.metadata.domain.model.events.BookLoadedEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.LoadBookUseCase;
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
public class LoadBookUseCaseImpl implements LoadBookUseCase {

	@Resource
	private UtilComponent utilComponent;

	@Resource
	protected TagRepository tagRepository;

	@Resource
	protected BookRepository bookRepository;

	@Resource
	protected CalibreRepository calibreRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Resource
	protected EventBus eventBus;

	@Override
	@Transactional
	public void load(final String bookId, final boolean override) {

		final Book book = calibreRepository.findBookById(bookId);

		if (override || bookRepository.findByPath(book.getPath()).isEmpty()) {

			final String image = utilComponent.getBase64Cover(book.getPath(), true);
			book.setImage(image);
			book.setId(null);

			//Save tags
			book.getTags().forEach(tag -> {
				final Optional<TagMongoEntity> optTagEntity = tagRepository.findByName(tag).stream().findFirst();
				if (optTagEntity.isEmpty()) {
					tagRepository.save(new TagMongoEntity(tag, book.getLanguages()));
				}
				else {
					final TagMongoEntity tagEntity = optTagEntity.get();
					book.getLanguages().forEach(language -> {
						if (tagEntity.getNumBooks().getLanguages().get(language) != null) {
							tagEntity.getNumBooks().getLanguages().put(language, tagEntity.getNumBooks().getLanguages().get(language) + 1);
						}
						else {
							tagEntity.getNumBooks().getLanguages().put(language, 1);
						}
					});
					tagEntity.getNumBooks().setTotal(tagEntity.getNumBooks().getTotal() + 1);
					tagRepository.save(tagEntity);
				}
			});

			bookRepository.save(bookMongoMapper.domain2Entity(book));

			log.info("Book '{}' saved to database", book.getTitle());

			eventBus.publish(BookLoadedEvent.builder().bookId(bookId).build());
		}

	}

}
