package com.martinia.indigo.file.application.events;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.domain.model.BookOpf;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.model.events.EpubFileAddedEvent;
import com.martinia.indigo.file.domain.ports.usecases.events.SaveBookEpubFileExtractedEventUseCase;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class SaveBookEpubFileExtractedEventUseCaseImpl implements SaveBookEpubFileExtractedEventUseCase {

	@Resource
	private BookRepository bookRepository;
	@Resource
	private TagRepository tagRepository;

	@Resource
	private EventBus eventBus;

	@Value("${book.library.path}")
	private String endpointBook;

	@Resource
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;

	@Override
	@Transactional
	public void save(final BookOpf bookOpf, final Path path) {

		final String authorPath = getAuthorPath(bookOpf.getAuthors());
		final String basePath =
				endpointBook + FileSystems.getDefault().getSeparator() + authorPath + FileSystems.getDefault().getSeparator()
						+ bookOpf.getTitle() + " (" + bookOpf.getLanguage() + ")";

		final BookMongoEntity entity = bookRepository.findByPath(basePath).map(bookMongoEntity -> {
			if (bookMongoEntity.getVersion() < bookOpf.getVersion()) {
				bookMongoEntity.setComment(bookOpf.getComment());
				bookMongoEntity.setSerie(Optional.ofNullable(bookOpf.getSeriesName())
						.map(s -> SerieMongo.builder().name(bookOpf.getSeriesName()).index(bookOpf.getSeriesIndex()).build())
						.orElse(null));
				bookMongoEntity.setPubDate(bookOpf.getPubDate());
				bookMongoEntity.setLastModified(bookOpf.getLastModified());
				bookMongoEntity.setPages(bookOpf.getPages());
				bookMongoEntity.setImage(bookOpf.getBookImage());
				bookMongoEntity.setAuthors(bookOpf.getAuthors());
				bookMongoEntity.setVersion(bookOpf.getVersion());
			}
			uploadEpubFilesSingleton.addUpdatedBook();
			return bookMongoEntity;
		}).orElseGet(() -> {

			final BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
					.id(null)
					.title(bookOpf.getTitle())
					.path(basePath)
					.comment(bookOpf.getComment())
					.serie(Optional.ofNullable(bookOpf.getSeriesName())
							.map(s -> SerieMongo.builder().name(bookOpf.getSeriesName()).index(bookOpf.getSeriesIndex()).build())
							.orElse(null))
					.pubDate(bookOpf.getPubDate())
					.lastModified(bookOpf.getLastModified())
					.pages(bookOpf.getPages())
					.rating(0)
					.image(bookOpf.getBookImage())
					.authors(bookOpf.getAuthors())
					.tags(bookOpf.getTags())
					.languages(List.of(bookOpf.getLanguage()))
					.lastMetadataSync(null)
					.version(bookOpf.getVersion())
					.build();
			uploadEpubFilesSingleton.addNewBook();
			return bookMongoEntity;
		});

		bookRepository.save(entity);


		eventBus.publish(EpubFileAddedEvent.builder().bookId(entity.getId()).authorImage(bookOpf.getAuthorImage()).sourcePath(path).targetPath(Paths.get(basePath)).build());
	}

	private static String getAuthorPath(final List<String> authors) {
		String authorPath = "AA. VV.";
		if (authors.size() == 1 && !authors.get(0).toUpperCase().contains("AA. VV") && !authors.get(0).contains("& ")) {
			authorPath = authors.get(0);
		}
		return authorPath;
	}

}




