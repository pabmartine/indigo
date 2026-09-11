package com.martinia.indigo.file.application.events;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.domain.model.BookOpf;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.FileRepository;
import com.martinia.indigo.file.domain.model.File;
import com.martinia.indigo.file.domain.model.events.EpubFileAddedEvent;
import com.martinia.indigo.file.domain.ports.usecases.events.SaveBookEpubFileExtractedEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import com.martinia.indigo.file.application.EpubImportPolicy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class SaveBookEpubFileExtractedEventUseCaseImpl implements SaveBookEpubFileExtractedEventUseCase {

	@Resource
	private BookRepository bookRepository;
	@Resource
	private EventBus eventBus;

	@Value("${book.library.path}")
	private String endpointBook;
	@Value("${book.library.uploads}")
	private String uploadsPath;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.file.application.ImportRecoveryJournal recoveryJournal;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.file.application.PendingImportService pendingImports;

	@Resource
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;

	@Resource
	private FileRepository fileRepository;

	@Override
	@Transactional
	public synchronized void save(final BookOpf bookOpf, final Path path) {

		final Path libraryRoot = Path.of(endpointBook).toAbsolutePath().normalize();
		final String authorPath = sanitizePathSegment(getAuthorPath(bookOpf.getAuthors()));
		final String title = sanitizePathSegment(bookOpf.getTitle());
		final String language = sanitizePathSegment(bookOpf.getLanguage());
		final Path targetPath = libraryRoot.resolve(authorPath).resolve(title + " (" + language + ")").normalize();
		if (!targetPath.startsWith(libraryRoot)) {
			log.error("Ignoring EPUB with an invalid target path: {}", path);
			uploadEpubFilesSingleton.addMoveError();
			return;
		}
		final String basePath = targetPath.toString();
		final var isbn = EpubImportPolicy.isbnKeys(bookOpf.getIsbn10(), bookOpf.getIsbn13());
		final var matches = isbn.isEmpty() ? List.<BookMongoEntity>of() : bookRepository.findByAnyIsbn(isbn).stream()
				.filter(book -> !EpubImportPolicy.title(bookOpf.getTitle()).isEmpty()
						&& EpubImportPolicy.title(book.getTitle()).equals(EpubImportPolicy.title(bookOpf.getTitle())))
				.toList();
		if (matches.size() > 1) {
			throw new IllegalStateException("Multiple existing books match ISBN and title; refusing ambiguous import");
		}
		var existing = matches.isEmpty() ? bookRepository.findByPath(basePath) : Optional.of(matches.get(0));
		if (existing.isEmpty() && bookOpf.getTitle() != null
				&& bookOpf.getAuthors() != null && !bookOpf.getAuthors().isEmpty() && bookOpf.getLanguage() != null) {
			var candidates = bookRepository.findByTitleIgnoreCase(bookOpf.getTitle().trim()).stream()
					.filter(book -> isbn.isEmpty() || EpubImportPolicy.isbnKeys(book.getIsbn10(), book.getIsbn13()).isEmpty())
					.filter(book -> book.getAuthors() != null && book.getLanguages() != null
							&& book.getLanguages().contains(bookOpf.getLanguage())
							&& book.getAuthors().stream().map(EpubImportPolicy::title).collect(java.util.stream.Collectors.toSet())
							.equals(bookOpf.getAuthors().stream().map(EpubImportPolicy::title).collect(java.util.stream.Collectors.toSet())))
					.toList();
			if (candidates.size() > 1) throw new IllegalStateException("Ambiguous title/author/language identity without ISBN");
			if (candidates.size() == 1) existing = Optional.of(candidates.get(0));
		}
		if (existing.isPresent()) {
			final var previous = existing.get();
			final var previousIsbn = EpubImportPolicy.isbnKeys(previous.getIsbn10(), previous.getIsbn13());
			if (!isbn.isEmpty() && !previousIsbn.isEmpty() && java.util.Collections.disjoint(isbn, previousIsbn)) {
				throw new IllegalStateException("Different ISBNs share the library path; refusing to merge editions");
			}
			if (!EpubImportPolicy.title(previous.getTitle()).equals(EpubImportPolicy.title(bookOpf.getTitle()))) {
				throw new IllegalStateException("Different titles share the library path; refusing to merge books");
			}
			if (isbn.isEmpty() || previousIsbn.isEmpty()) {
				if (previous.getAuthors() == null || bookOpf.getAuthors() == null || bookOpf.getAuthors().isEmpty()
						|| previous.getLanguages() == null || !previous.getLanguages().contains(bookOpf.getLanguage())
						|| !previous.getAuthors().stream().map(EpubImportPolicy::title).collect(java.util.stream.Collectors.toSet())
						.equals(bookOpf.getAuthors().stream().map(EpubImportPolicy::title).collect(java.util.stream.Collectors.toSet()))) {
					throw new IllegalStateException("Insufficient identity evidence without ISBN; upload retained");
				}
			}
			importExisting(previous, bookOpf, path, libraryRoot);
			return;
		}
		// Reject conflicting content before updating the database. A filename is not a duplicate identity.
		final Path targetFile = targetPath.resolve(path.getFileName());
		try {
			if (java.nio.file.Files.exists(targetFile)
					&& (java.nio.file.Files.isSameFile(path, targetFile)
					|| java.nio.file.Files.mismatch(path, targetFile) != -1)) {
				log.error("Import conflict; EPUB and existing metadata preserved: {}", path);
				uploadEpubFilesSingleton.addMoveError();
				return;
			}
		}
		catch (java.io.IOException exception) {
			throw new IllegalStateException("Cannot compare existing EPUB with " + path, exception);
		}
		com.martinia.indigo.file.application.ImportExecution.prepareNewBookImages();
		final BookMongoEntity entity = BookMongoEntity.builder()
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
					.isbn10(bookOpf.getIsbn10())
					.isbn13(bookOpf.getIsbn13())
					.identifiers(bookOpf.getIdentifiers())
					.languages(List.of(bookOpf.getLanguage()))
					.lastMetadataSync(null)
					.version(bookOpf.getVersion())
					.build();

		BookMongoEntity savedEntity = bookRepository.save(entity);
		if (savedEntity == null) {
			return;
		}
		uploadEpubFilesSingleton.addNewBook();
		// File entities have their own UUID identity. The book Mongo id is an
		// ObjectId string and must not be parsed as a UUID.
		// Track the upload path so the move step can remove this transient record.
		// The library destination is already represented by the Book document.
		fileRepository.save(File.builder().id(UUID.randomUUID()).path(path).build());


		final EpubFileAddedEvent event = EpubFileAddedEvent.builder()
				.bookId(savedEntity.getId())
				.authorImage(bookOpf.getAuthorImage())
				.sourcePath(path)
				.targetPath(targetPath)
				.newBook(true)
				.build();
		publishAfterCommit(event);
	}

	private void importExisting(BookMongoEntity book, BookOpf incoming, Path source, Path libraryRoot) {
		if (!EpubImportPolicy.upgrade(incoming.getVersion(), book.getVersion())) {
			// A database row alone is not proof that the initial import installed its file.
			try (var installed = Files.list(Path.of(book.getPath()))) {
				if (installed.noneMatch(file -> Files.isRegularFile(file) && file.toString().toLowerCase(java.util.Locale.ROOT).endsWith(".epub"))) {
					throw new IOException("Existing EPUB is missing; incoming file retained for recovery");
				}
			} catch (IOException exception) { throw new IllegalStateException("Initial import is incomplete; incoming file retained", exception); }
			// No metadata changes, including identifiers or cover, on a discarded import.
			afterCommit(() -> discard(source, false));
			return;
		}
		Path directory = Path.of(book.getPath()).toAbsolutePath().normalize();
		if (!directory.startsWith(libraryRoot)) throw new IllegalStateException("Book path outside library");
		Path backup = null;
		Path target = null;
		float previousVersion = book.getVersion();
		try {
			if (!source.toRealPath().startsWith(Path.of(uploadsPath).toRealPath())
					|| source.toRealPath().startsWith(libraryRoot.toRealPath())
					|| !directory.toRealPath().startsWith(libraryRoot.toRealPath())) {
				throw new IOException("Invalid EPUB source or destination");
			}
			try (var files = Files.list(directory)) {
				var epubs = files.filter(Files::isRegularFile)
						.filter(file -> file.toString().toLowerCase(java.util.Locale.ROOT).endsWith(".epub")).toList();
				if (epubs.size() != 1) throw new IOException("Expected exactly one existing EPUB in " + directory);
				target = epubs.get(0);
			}
			if (Files.isSameFile(source, target)) throw new IOException("Upload refers to the library EPUB itself");
			Path backupCandidate = directory.resolve(".import-" + UUID.randomUUID() + ".bak");
			Files.copy(target, backupCandidate);
			backup = backupCandidate;
			if (recoveryJournal != null) recoveryJournal.prepare(book.getId(), incoming.getVersion(), source, target, backup);
			Path staged = Files.createTempFile(directory, ".import-", ".tmp");
			try {
				Files.copy(source, staged, StandardCopyOption.REPLACE_EXISTING);
				try { Files.move(staged, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
				catch (java.nio.file.AtomicMoveNotSupportedException exception) {
					Files.move(staged, target, StandardCopyOption.REPLACE_EXISTING);
				}
			} finally { Files.deleteIfExists(staged); }
			// Keep personal data, enrichment, relationships and their counters unchanged.
			book.setVersion(incoming.getVersion());
			if (bookRepository.save(book) == null) throw new IllegalStateException("Book update was not saved");
			final Path oldFile = backup;
			final Path installedFile = target;
			if (TransactionSynchronizationManager.isActualTransactionActive()) {
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
					@Override
					public void afterCompletion(int status) {
						if (status == STATUS_COMMITTED) finishUpgrade(source, oldFile);
						else restore(oldFile, installedFile);
					}
				});
			} else finishUpgrade(source, backup);
		}
		catch (Exception exception) {
			book.setVersion(previousVersion);
			if (backup != null && target != null) restore(backup, target);
			throw new IllegalStateException("Could not update EPUB; incoming file preserved", exception);
		}
	}

	private void afterCommit(Runnable action) {
		if (!TransactionSynchronizationManager.isActualTransactionActive()) action.run();
		else TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override public void afterCommit() { action.run(); }
		});
	}

	private void restore(Path backup, Path target) {
		try {
			Files.copy(backup, target, StandardCopyOption.REPLACE_EXISTING);
			Files.delete(backup);
			Files.deleteIfExists(com.martinia.indigo.file.application.ImportRecoveryJournal.marker(backup));
		} catch (IOException exception) {
			log.error("Could not restore EPUB; recovery copy retained at {}", backup, exception);
		}
	}

	private void finishUpgrade(Path source, Path backup) {
		try {
			Files.deleteIfExists(backup);
			Files.deleteIfExists(com.martinia.indigo.file.application.ImportRecoveryJournal.marker(backup));
		}
		catch (IOException exception) { log.warn("Could not remove temporary EPUB backup {}", backup, exception); }
		discard(source, true);
	}

	private void discard(Path source, boolean updated) {
		try {
			Path uploadRoot = Path.of(uploadsPath).toRealPath();
			Path realSource = source.toRealPath();
			if (!realSource.startsWith(uploadRoot) || realSource.startsWith(Path.of(endpointBook).toRealPath())) {
				throw new IOException("Refusing to discard a file outside uploads or inside library");
			}
			Files.delete(source);
			if (updated) uploadEpubFilesSingleton.addUpdatedBook();
			uploadEpubFilesSingleton.addMove();
			log.info("{} incoming EPUB {}", updated ? "Updated existing book from" : "Discarded duplicate", source);
		} catch (IOException exception) {
			uploadEpubFilesSingleton.addMoveError();
			log.error("Could not discard processed upload {}", source, exception);
		}
	}

	private void publishAfterCommit(final EpubFileAddedEvent event) {
		if (pendingImports != null) pendingImports.register(event);
		if (!TransactionSynchronizationManager.isActualTransactionActive()) {
			if (!com.martinia.indigo.file.application.ImportExecution.capture(event)) eventBus.publish(event);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				if (!com.martinia.indigo.file.application.ImportExecution.capture(event)) eventBus.publish(event);
			}
		});
	}

	private static String getAuthorPath(final List<String> authors) {
		String authorPath = "AA. VV.";
		if (authors != null && authors.size() == 1 && authors.get(0) != null && !authors.get(0).toUpperCase().contains("AA. VV") && !authors.get(0).contains("& ")) {
			authorPath = authors.get(0);
		}
		return authorPath;
	}

	private static String sanitizePathSegment(final String value) {
		if (value == null || value.isBlank()) {
			return "Unknown";
		}
		final String sanitized = value.trim().replaceAll("[\\\\/]", "-");
		return ".".equals(sanitized) || "..".equals(sanitized) ? "Unknown" : sanitized;
	}

}
