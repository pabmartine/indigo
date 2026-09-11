package com.martinia.indigo.metadata.application.openlibrary;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.metadata.domain.model.OpenLibraryDownloadResult;
import com.martinia.indigo.metadata.domain.model.OpenLibraryIndexCancelledException;
import com.martinia.indigo.metadata.domain.model.OpenLibraryIndexJobStatus;
import com.martinia.indigo.metadata.domain.model.OpenLibraryRemoteFile;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.OpenLibraryDumpDownloadPort;
import com.martinia.indigo.metadata.domain.ports.repositories.OpenLibraryEditionMappingRepository;
import com.martinia.indigo.metadata.domain.ports.repositories.OpenLibraryIndexJobRepository;
import com.martinia.indigo.metadata.domain.ports.repositories.OpenLibraryIndexVersionRepository;
import com.martinia.indigo.metadata.domain.ports.repositories.OpenLibraryRatingRepository;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryIndexJobMongoEntity;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryIndexVersionMongoEntity;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OpenLibraryIndexManager {
	public static final String JOB_ID = "open-library-index";
	private static final DateTimeFormatter VERSION_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
			.withZone(ZoneOffset.UTC);
	private static final long PROGRESS_SAVE_INTERVAL_NANOS = 1_000_000_000L;

	@Resource
	private OpenLibraryIndexJobRepository jobRepository;
	@Resource
	private OpenLibraryIndexVersionRepository versionRepository;
	@Resource
	private OpenLibraryEditionMappingRepository mappingRepository;
	@Resource
	private OpenLibraryRatingRepository ratingRepository;
	@Resource
	private BookRepository bookRepository;
	@Resource
	private OpenLibraryDumpDownloadPort downloadPort;
	@Resource
	private OpenLibraryDumpProcessor processor;
	@Resource
	private MongoTemplate mongoTemplate;

	@Value("${metadata.openlibrary.dumps.editions-url}")
	private String editionsUrl;
	@Value("${metadata.openlibrary.dumps.ratings-url}")
	private String ratingsUrl;
	@Value("${metadata.openlibrary.dumps.storage-path}")
	private String storagePath;
	@Value("${metadata.openlibrary.dumps.minimum-free-space-bytes:1073741824}")
	private long minimumFreeSpaceBytes;
	@Value("${metadata.openlibrary.dumps.delete-downloads-on-success:true}")
	private boolean deleteDownloadsOnSuccess;

	private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
		final Thread thread = new Thread(runnable, "open-library-indexer");
		thread.setDaemon(true);
		return thread;
	});
	private final AtomicBoolean cancellation = new AtomicBoolean();
	private final AtomicBoolean executing = new AtomicBoolean();
	private volatile boolean shuttingDown;

	@PostConstruct
	void resumeInterruptedJob() {
		jobRepository.findById(JOB_ID)
				.filter(job -> job.getStatus() != null && job.getStatus().isRunning())
				.ifPresent(this::submit);
	}

	@PreDestroy
	void shutdown() {
		shuttingDown = true;
		executor.shutdownNow();
	}

	public synchronized OpenLibraryIndexJobMongoEntity start() {
		final OpenLibraryIndexJobMongoEntity current = jobRepository.findById(JOB_ID).orElse(null);
		if (current != null && (executing.get()
				|| (current.getStatus() != null && current.getStatus().isRunning()))) {
			return current;
		}
		final String version = VERSION_FORMAT.format(Instant.now()) + '-' + UUID.randomUUID().toString().substring(0, 8);
		final Path downloads = storageRoot().resolve("downloads").resolve(version);
		final Date now = new Date();
		final OpenLibraryIndexJobMongoEntity job = OpenLibraryIndexJobMongoEntity.builder()
				.id(JOB_ID)
				.status(OpenLibraryIndexJobStatus.CHECKING_SPACE)
				.stagingVersion(version)
				.activeVersion(current == null || current.getActiveVersion() == null
						? activeVersion()
						: current.getActiveVersion())
				.editionsUrl(editionsUrl)
				.ratingsUrl(ratingsUrl)
				.editionsFile(downloads.resolve("editions.txt.gz").toString())
				.ratingsFile(downloads.resolve("ratings.txt.gz").toString())
				.cancelRequested(false)
				.deleteDownloadsOnSuccess(deleteDownloadsOnSuccess)
				.requestedAt(now)
				.startedAt(now)
				.updatedAt(now)
				.build();
		jobRepository.save(job);
		submit(job);
		return job;
	}

	public synchronized OpenLibraryIndexJobMongoEntity resume() {
		final OpenLibraryIndexJobMongoEntity job = jobRepository.findById(JOB_ID).orElseThrow(
				() -> new IllegalStateException("No Open Library index job exists"));
		if (executing.get() || (job.getStatus() != null && job.getStatus().isRunning())) {
			return job;
		}
		if (job.getStatus() == OpenLibraryIndexJobStatus.COMPLETED) {
			return job;
		}
		job.setStatus(OpenLibraryIndexJobStatus.CHECKING_SPACE);
		job.setCancelRequested(false);
		job.setError(null);
		job.setCompletedAt(null);
		job.setUpdatedAt(new Date());
		jobRepository.save(job);
		submit(job);
		return job;
	}

	public synchronized OpenLibraryIndexJobMongoEntity cancel() {
		final OpenLibraryIndexJobMongoEntity job = jobRepository.findById(JOB_ID).orElseThrow(
				() -> new IllegalStateException("No Open Library index job exists"));
		if (job.getStatus() != null && job.getStatus().isRunning()) {
			cancellation.set(true);
			job.setCancelRequested(true);
			job.setUpdatedAt(new Date());
			jobRepository.save(job);
		}
		return job;
	}

	public OpenLibraryIndexJobMongoEntity status() {
		return jobRepository.findById(JOB_ID).orElseGet(() -> OpenLibraryIndexJobMongoEntity.builder()
				.id(JOB_ID)
				.status(OpenLibraryIndexJobStatus.IDLE)
				.activeVersion(activeVersion())
				.build());
	}

	boolean isExecuting() {
		return executing.get();
	}

	private void submit(final OpenLibraryIndexJobMongoEntity job) {
		if (executing.compareAndSet(false, true)) {
			cancellation.set(job.isCancelRequested());
			executor.submit(() -> {
				try {
					run(job.getStagingVersion());
				}
				finally {
					executing.set(false);
				}
			});
		}
	}

	private void run(final String version) {
		OpenLibraryIndexJobMongoEntity job = jobRepository.findById(JOB_ID).orElseThrow();
		try {
			job.setStatus(OpenLibraryIndexJobStatus.CHECKING_SPACE);
			job.setStartedAt(job.getStartedAt() == null ? new Date() : job.getStartedAt());
			job.setUpdatedAt(new Date());
			jobRepository.save(job);

			final OpenLibraryRemoteFile editionsRemote = downloadPort.inspect(job.getEditionsUrl());
			final OpenLibraryRemoteFile ratingsRemote = downloadPort.inspect(job.getRatingsUrl());
			final Path editionsFile = safeJobFile(job.getEditionsFile());
			final Path ratingsFile = safeJobFile(job.getRatingsFile());
			ensureFreeSpace(editionsRemote.size(), ratingsRemote.size(), editionsFile, ratingsFile);
			job.setTotalBytes(saturatingAdd(positive(editionsRemote.size()), positive(ratingsRemote.size())));
			jobRepository.save(job);

			final Set<String> libraryIsbns = libraryIsbns();
			job.setLibraryIsbns(libraryIsbns.size());
			job.setStatus(OpenLibraryIndexJobStatus.DOWNLOADING_EDITIONS);
			jobRepository.save(job);
			ProgressPersistence progress = new ProgressPersistence(job, 0L);
			final OpenLibraryDownloadResult editionsDownload = downloadPort.download(job.getEditionsUrl(), editionsFile,
					cancellation::get, progress::downloaded);
			job.setEditionsLastModified(firstNonBlank(editionsDownload.lastModified(), editionsRemote.lastModified()));

			job.setStatus(OpenLibraryIndexJobStatus.PROCESSING_EDITIONS);
			job.setProcessedRecords(0L);
			job.setMatchedRecords(0L);
			jobRepository.save(job);
			progress = new ProgressPersistence(job, 0L);
			final OpenLibraryDumpProcessor.EditionProcessingResult editions = processor.processEditions(editionsFile,
					version, libraryIsbns, cancellation::get, progress::processed);
			job.setMatchedIsbns(editions.matchedIsbns().size());
			job.setMatchedWorks(editions.matchedWorks().size());
			job.setMatchedRecords(editions.matchedIsbns().size());

			job.setStatus(OpenLibraryIndexJobStatus.DOWNLOADING_RATINGS);
			job.setDownloadedBytes(editionsDownload.size());
			jobRepository.save(job);
			progress = new ProgressPersistence(job, editionsDownload.size());
			final OpenLibraryDownloadResult ratingsDownload = downloadPort.download(job.getRatingsUrl(), ratingsFile,
					cancellation::get, progress::downloaded);
			job.setRatingsLastModified(firstNonBlank(ratingsDownload.lastModified(), ratingsRemote.lastModified()));

			job.setStatus(OpenLibraryIndexJobStatus.PROCESSING_RATINGS);
			job.setProcessedRecords(0L);
			jobRepository.save(job);
			progress = new ProgressPersistence(job, 0L);
			final OpenLibraryDumpProcessor.RatingProcessingResult ratings = processor.processRatings(ratingsFile, version,
					editions.matchedWorks(), cancellation::get, progress::processed);
			job.setWorksWithRatings(ratings.worksWithRatings());

			checkCancellation();
			job.setStatus(OpenLibraryIndexJobStatus.ACTIVATING);
			jobRepository.save(job);
			versionRepository.save(OpenLibraryIndexVersionMongoEntity.builder()
					.version(version)
					.active(false)
					.createdAt(job.getRequestedAt())
					.editionsSource(job.getEditionsUrl())
					.ratingsSource(job.getRatingsUrl())
					.editionsLastModified(job.getEditionsLastModified())
					.ratingsLastModified(job.getRatingsLastModified())
					.libraryIsbns(libraryIsbns.size())
					.matchedIsbns(editions.matchedIsbns().size())
					.matchedWorks(editions.matchedWorks().size())
					.worksWithRatings(ratings.worksWithRatings())
					.processedEditions(editions.processedRecords())
					.processedRatings(ratings.processedRecords())
					.build());

			job.setActiveVersion(version);
			job.setStatus(OpenLibraryIndexJobStatus.COMPLETED);
			job.setDownloadedBytes(editionsDownload.size() + ratingsDownload.size());
			job.setProcessedRecords(ratings.processedRecords());
			job.setCancelRequested(false);
			job.setError(null);
			job.setCompletedAt(new Date());
			job.setUpdatedAt(new Date());
			jobRepository.save(job);
			synchronizeVersionFlags(version);
			if (job.isDeleteDownloadsOnSuccess()) {
				deleteJobFiles(job);
			}
			log.info("Activated Open Library index {} with {} ISBNs, {} works and {} ratings", version,
					editions.matchedIsbns().size(), editions.matchedWorks().size(), ratings.worksWithRatings());
		}
		catch (OpenLibraryIndexCancelledException exception) {
			cleanupStaging(version);
			deleteJobFiles(job);
			job.setStatus(OpenLibraryIndexJobStatus.CANCELLED);
			job.setCancelRequested(false);
			job.setError(null);
			job.setCompletedAt(new Date());
			job.setUpdatedAt(new Date());
			jobRepository.save(job);
		}
		catch (RuntimeException exception) {
			if (shuttingDown) {
				log.info("Open Library index job {} interrupted by shutdown and will resume on startup", version);
				return;
			}
			cleanupStaging(version);
			job.setStatus(OpenLibraryIndexJobStatus.FAILED);
			job.setError(exception.getMessage());
			job.setCompletedAt(new Date());
			job.setUpdatedAt(new Date());
			jobRepository.save(job);
			log.error("Open Library index job {} failed", version, exception);
		}
	}

	private Set<String> libraryIsbns() {
		final Set<String> result = new HashSet<>();
		for (BookMongoEntity book : bookRepository.findBooksWithIsbn()) {
			addIsbns(result, book.getIsbn10());
			addIsbns(result, book.getIsbn13());
		}
		return result;
	}

	private void addIsbns(final Set<String> target, final List<String> values) {
		if (values != null) {
			values.stream().filter(value -> value != null && !value.isBlank())
					.map(value -> value.replaceAll("[^0-9Xx]", "").toUpperCase()).forEach(target::add);
		}
	}

	private void ensureFreeSpace(final long editionsSize, final long ratingsSize, final Path... files) {
		try {
			Files.createDirectories(storageRoot());
			long existing = 0L;
			for (Path file : files) {
				existing += existingSize(file);
			}
			final long remoteSize = saturatingAdd(positive(editionsSize), positive(ratingsSize));
			final long remaining = Math.max(0L, remoteSize - existing);
			final long required = saturatingAdd(remaining, positive(minimumFreeSpaceBytes));
			final FileStore fileStore = Files.getFileStore(storageRoot());
			if (fileStore.getUsableSpace() < required) {
				throw new IllegalStateException("Insufficient free space for Open Library dumps: required " + required
						+ " bytes, available " + fileStore.getUsableSpace() + " bytes");
			}
		}
		catch (IOException exception) {
			throw new IllegalStateException("Could not check storage space for Open Library dumps", exception);
		}
	}

	private long existingSize(final Path target) throws IOException {
		if (Files.isRegularFile(target)) {
			return Files.size(target);
		}
		final Path partial = target.resolveSibling(target.getFileName() + ".part");
		return Files.isRegularFile(partial) ? Files.size(partial) : 0L;
	}

	private void synchronizeVersionFlags(final String version) {
		try {
			mongoTemplate.updateMulti(new Query(Criteria.where("active").is(true)), Update.update("active", false),
					OpenLibraryIndexVersionMongoEntity.class);
			mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(version)),
					new Update().set("active", true).set("activatedAt", new Date()),
					OpenLibraryIndexVersionMongoEntity.class);
		}
		catch (RuntimeException exception) {
			// The job document's activeVersion is the canonical atomic pointer. These flags
			// are only maintained for inspection and must never roll back a published index.
			log.warn("Open Library index {} was activated, but version flags could not be synchronized: {}", version,
					exception.getMessage());
		}
	}

	private void cleanupStaging(final String version) {
		mappingRepository.deleteByIndexVersion(version);
		ratingRepository.deleteByIndexVersion(version);
		versionRepository.deleteById(version);
	}

	private void deleteJobFiles(final OpenLibraryIndexJobMongoEntity job) {
		deleteJobFile(job.getEditionsFile());
		deleteJobFile(job.getRatingsFile());
	}

	private void deleteJobFile(final String value) {
		if (value == null) {
			return;
		}
		try {
			final Path target = safeJobFile(value);
			Files.deleteIfExists(target);
			Files.deleteIfExists(target.resolveSibling(target.getFileName() + ".part"));
		}
		catch (IOException exception) {
			log.warn("Could not delete Open Library temporary file {}: {}", value, exception.getMessage());
		}
	}

	private Path safeJobFile(final String value) {
		final Path root = storageRoot();
		final Path target = Path.of(value).toAbsolutePath().normalize();
		if (!target.startsWith(root)) {
			throw new IllegalArgumentException("Open Library dump path is outside the configured storage directory");
		}
		return target;
	}

	private Path storageRoot() {
		return Path.of(storagePath).toAbsolutePath().normalize();
	}

	private String activeVersion() {
		return versionRepository.findFirstByActiveTrue().map(OpenLibraryIndexVersionMongoEntity::getVersion).orElse(null);
	}

	private void checkCancellation() {
		if (cancellation.get()) {
			throw new OpenLibraryIndexCancelledException();
		}
	}

	private long positive(final long value) {
		return Math.max(0L, value);
	}

	private long saturatingAdd(final long first, final long second) {
		return first > Long.MAX_VALUE - second ? Long.MAX_VALUE : first + second;
	}

	private String firstNonBlank(final String first, final String second) {
		return first == null || first.isBlank() ? second : first;
	}

	private final class ProgressPersistence {
		private final OpenLibraryIndexJobMongoEntity job;
		private final long base;
		private long lastSaved;

		private ProgressPersistence(final OpenLibraryIndexJobMongoEntity job, final long base) {
			this.job = job;
			this.base = base;
		}

		private void downloaded(final long bytes) {
			job.setDownloadedBytes(base + bytes);
			persist();
		}

		private void processed(final long records) {
			job.setProcessedRecords(records);
			persist();
		}

		private void persist() {
			final long now = System.nanoTime();
			if (now - lastSaved >= PROGRESS_SAVE_INTERVAL_NANOS) {
				job.setCancelRequested(cancellation.get());
				job.setUpdatedAt(new Date());
				jobRepository.save(job);
				lastSaved = now;
			}
		}
	}
}
