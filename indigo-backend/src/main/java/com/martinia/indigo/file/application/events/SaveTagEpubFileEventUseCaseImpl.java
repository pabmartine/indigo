package com.martinia.indigo.file.application.events;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.domain.model.BookOpf;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.common.util.XmlUtils;
import com.martinia.indigo.file.domain.ports.usecases.events.SaveTagEpubFileEventUseCase;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
@Slf4j
public class SaveTagEpubFileEventUseCaseImpl implements SaveTagEpubFileEventUseCase {

	@Resource
	private BookRepository bookRepository;
	@Resource
	private TagRepository tagRepository;
	@Resource
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;
	@Value("${book.library.path}")
	private String libraryPath;
	@Value("${book.library.reconcile-categories-on-startup:true}")
	private boolean reconcileCategoriesOnStartup;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.file.application.PendingImportService pendingImports;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.metadata.application.MetadataActivityService activity;

	@Override
	public synchronized void save(final String bookId) { save(bookId, true); }

	@Override
	public synchronized void save(final String bookId, final boolean newBook) {
		if (pendingImports != null && pendingImports.done(bookId, "tagsDone")) return;
		rebuildAfterBatch(List.of(bookId));
	}

	@Override
	public synchronized void rebuildAfterBatch(final List<String> bookIds) {
		rebuildCatalog();
		if (pendingImports != null) bookIds.forEach(id -> pendingImports.complete(id, "tagsDone"));
	}

	@EventListener(ApplicationReadyEvent.class)
	public synchronized void rebuildOnStartup() {
		if (reconcileCategoriesOnStartup) reconcileBookMetadata();
		rebuildCatalog();
	}

	private void reconcileBookMetadata() {
		final Path root = Path.of(libraryPath).toAbsolutePath().normalize();
		int repairedBooks = 0;
		for (BookMongoEntity book : bookRepository.findAll()) {
			if (activity != null && activity.isLocked("BOOKS", book.getId())) continue;
			final Path bookDirectory = Path.of(book.getPath()).toAbsolutePath().normalize();
			if (!bookDirectory.startsWith(root) || !Files.isDirectory(bookDirectory)) {
				continue;
			}
			final Optional<BookOpf> parsedBook = readBookMetadata(bookDirectory);
			if (parsedBook.isEmpty()) {
				continue;
			}
			final BookOpf bookOpf = parsedBook.get();
			boolean changed = false;
			final List<String> categories = normalizeTags(bookOpf.getTags());
			if (!categories.isEmpty() && !Objects.equals(categories, normalizeTags(book.getTags()))) {
				book.setTags(categories);
				changed = true;
			}
			if (bookOpf.getIdentifiers() != null && !bookOpf.getIdentifiers().isEmpty()
					&& (!Objects.equals(bookOpf.getIdentifiers(), book.getIdentifiers())
							|| !Objects.equals(bookOpf.getIsbn10(), book.getIsbn10())
							|| !Objects.equals(bookOpf.getIsbn13(), book.getIsbn13()))) {
				book.setIdentifiers(bookOpf.getIdentifiers());
				book.setIsbn10(bookOpf.getIsbn10());
				book.setIsbn13(bookOpf.getIsbn13());
				book.setMetadataMatchStatus(null);
				book.setMetadataMatchConfidence(null);
				changed = true;
			}
			if (changed) {
				bookRepository.save(book);
				repairedBooks++;
			}
		}
		if (repairedBooks > 0) {
			log.info("Reconciled EPUB categories and identifiers for {} books", repairedBooks);
		}
	}

	private Optional<BookOpf> readBookMetadata(final Path bookDirectory) {
		try (var files = Files.list(bookDirectory)) {
			return files.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".epub"))
					.findFirst()
					.flatMap(this::readBookMetadataFromEpub);
		}
		catch (IOException exception) {
			log.warn("Unable to inspect EPUB directory {} while reconciling metadata", bookDirectory, exception);
			return Optional.empty();
		}
	}

	private Optional<BookOpf> readBookMetadataFromEpub(final Path epub) {
		try (ZipFile zipFile = new ZipFile(epub.toFile())) {
			final Optional<? extends ZipEntry> opfEntry = zipFile.stream()
					.filter(entry -> !entry.isDirectory())
					.filter(entry -> entry.getName().toLowerCase(Locale.ROOT).endsWith(".opf"))
					.findFirst();
			if (opfEntry.isEmpty()) {
				return Optional.empty();
			}
			try (InputStream input = zipFile.getInputStream(opfEntry.get())) {
				return Optional.ofNullable(XmlUtils.parse(input));
			}
		}
		catch (IOException exception) {
			log.warn("Unable to read metadata from EPUB {}", epub, exception);
			return Optional.empty();
		}
	}

	private static List<String> normalizeTags(final List<String> tags) {
		final Map<String, String> normalized = new LinkedHashMap<>();
		for (String tag : Optional.ofNullable(tags).orElseGet(List::of)) {
			if (StringUtils.isNotBlank(tag)) {
				final String value = tag.trim();
				normalized.putIfAbsent(value.toLowerCase(Locale.ROOT), value);
			}
		}
		return new ArrayList<>(normalized.values());
	}

	private void rebuildCatalog() {
		final Map<String, CategoryCount> categories = new LinkedHashMap<>();
		final List<BookMongoEntity> books = bookRepository.findAll();
		for (BookMongoEntity book : books) {
			for (String tag : normalizeTags(book.getTags())) {
				final CategoryCount count = categories.computeIfAbsent(tag.toLowerCase(Locale.ROOT), key -> new CategoryCount(tag));
				count.total++;
				for (String language : Optional.ofNullable(book.getLanguages()).orElseGet(List::of)) {
					if (StringUtils.isNotBlank(language)) {
						count.languages.merge(language, 1, Integer::sum);
					}
				}
			}
		}

		final Map<String, TagMongoEntity> existing = new HashMap<>();
		for (TagMongoEntity tag : tagRepository.findAll()) {
			existing.put(tag.getName().toLowerCase(Locale.ROOT), tag);
		}
		for (Map.Entry<String, CategoryCount> entry : categories.entrySet()) {
			final CategoryCount count = entry.getValue();
			final TagMongoEntity tag = Optional.ofNullable(existing.get(entry.getKey())).orElseGet(() -> {
				uploadEpubFilesSingleton.addTag();
				return TagMongoEntity.builder().name(count.name).build();
			});
			tag.setNumBooks(NumBooksMongo.builder().total(count.total).languages(count.languages).build());
			tagRepository.save(tag);
		}
		log.info("Category catalog synchronized: {} categories from {} books", categories.size(), books.size());
	}

	private static final class CategoryCount {
		private final String name;
		private int total;
		private final Map<String, Integer> languages = new HashMap<>();

		private CategoryCount(final String name) {
			this.name = name;
		}
	}

}
