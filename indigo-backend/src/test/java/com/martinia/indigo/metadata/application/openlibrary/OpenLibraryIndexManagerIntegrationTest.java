package com.martinia.indigo.metadata.application.openlibrary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.zip.GZIPOutputStream;

import com.martinia.indigo.BaseIndigoIntegrationTest;
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
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class OpenLibraryIndexManagerIntegrationTest extends BaseIndigoIntegrationTest {
	@TempDir
	private Path temporaryDirectory;

	@Resource
	private OpenLibraryIndexManager indexManager;
	@Resource
	private OpenLibraryIndexJobRepository jobRepository;
	@Resource
	private OpenLibraryIndexVersionRepository versionRepository;
	@Resource
	private OpenLibraryEditionMappingRepository mappingRepository;
	@Resource
	private OpenLibraryRatingRepository ratingRepository;
	@MockBean
	private OpenLibraryDumpDownloadPort downloadPort;

	private Path editionsFixture;
	private Path ratingsFixture;

	@BeforeEach
	void setUp() throws IOException {
		cleanDatabase();
		ReflectionTestUtils.setField(indexManager, "storagePath", temporaryDirectory.resolve("index").toString());
		ReflectionTestUtils.setField(indexManager, "minimumFreeSpaceBytes", 0L);
		ReflectionTestUtils.setField(indexManager, "deleteDownloadsOnSuccess", false);
		editionsFixture = gzip("editions-fixture.txt.gz", """
				/type/edition\t/books/OL1M\t1\t2026-08-01T00:00:00\t{"key":"/books/OL1M","isbn_13":["9780261102217"],"works":[{"key":"/works/OL1W"}]}
				/type/edition\t/books/OL2M\t1\t2026-08-01T00:00:00\t{"key":"/books/OL2M","isbn_13":["9780000000002"],"works":[{"key":"/works/OL2W"}]}
				""");
		ratingsFixture = gzip("ratings-fixture.txt.gz", """
				Work Key\tEdition Key\tRating\tDate
				/works/OL1W\t/books/OL1M\t4\t2026-08-01
				/works/OL1W\t/books/OL1M\t5\t2026-08-02
				""");
		when(downloadPort.inspect(anyString())).thenReturn(new OpenLibraryRemoteFile(100L, "2026-08-31", "etag"));
		when(downloadPort.download(anyString(), any(Path.class), any(), any())).thenAnswer(invocation -> {
			String url = invocation.getArgument(0);
			Path target = invocation.getArgument(1);
			Path source = url.contains("editions") ? editionsFixture : ratingsFixture;
			Files.createDirectories(target.getParent());
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			return new OpenLibraryDownloadResult(target, Files.size(target), "2026-08-31", "etag");
		});
		bookRepository.save(BookMongoEntity.builder()
				.title("The Hobbit")
				.path("hobbit.epub")
				.isbn13(List.of("9780261102217"))
				.build());
	}

	@AfterEach
	void tearDown() {
		cleanDatabase();
	}

	@Test
	void buildsAndAtomicallyActivatesANewIndexVersion() throws Exception {
		versionRepository.save(OpenLibraryIndexVersionMongoEntity.builder()
				.version("old-version")
				.active(true)
				.build());

		OpenLibraryIndexJobMongoEntity started = indexManager.start();
		OpenLibraryIndexJobMongoEntity completed = awaitTerminalStatus();

		assertThat(started.getStagingVersion()).isNotBlank();
		assertThat(completed.getStatus()).isEqualTo(OpenLibraryIndexJobStatus.COMPLETED);
		assertThat(completed.getActiveVersion()).isEqualTo(started.getStagingVersion());
		assertThat(completed.getLibraryIsbns()).isEqualTo(1);
		assertThat(completed.getMatchedIsbns()).isEqualTo(1);
		assertThat(completed.getMatchedWorks()).isEqualTo(1);
		assertThat(completed.getWorksWithRatings()).isEqualTo(1);
		assertThat(mappingRepository.findByIndexVersion(completed.getActiveVersion())).singleElement()
				.satisfies(mapping -> {
					assertThat(mapping.getIsbn()).isEqualTo("9780261102217");
					assertThat(mapping.getEditionId()).isEqualTo("OL1M");
					assertThat(mapping.getWorkId()).isEqualTo("OL1W");
				});
		assertThat(ratingRepository.findByIndexVersionAndWorkId(completed.getActiveVersion(), "OL1W"))
				.get().satisfies(rating -> {
					assertThat(rating.getAverage()).isEqualTo(4.5F);
					assertThat(rating.getCount()).isEqualTo(2);
				});
		assertThat(versionRepository.findById("old-version")).get()
				.extracting(OpenLibraryIndexVersionMongoEntity::isActive).isEqualTo(false);
		assertThat(versionRepository.findById(completed.getActiveVersion())).get()
				.extracting(OpenLibraryIndexVersionMongoEntity::isActive).isEqualTo(true);
	}

	@Test
	void keepsThePreviousIndexWhenAStagingDumpIsCorrupt() throws Exception {
		versionRepository.save(OpenLibraryIndexVersionMongoEntity.builder()
				.version("old-version")
				.active(true)
				.build());
		ratingsFixture = temporaryDirectory.resolve("corrupt-ratings.txt.gz");
		Files.writeString(ratingsFixture, "not a gzip file", StandardCharsets.UTF_8);

		OpenLibraryIndexJobMongoEntity started = indexManager.start();
		OpenLibraryIndexJobMongoEntity failed = awaitTerminalStatus();

		assertThat(failed.getStatus()).isEqualTo(OpenLibraryIndexJobStatus.FAILED);
		assertThat(failed.getActiveVersion()).isEqualTo("old-version");
		assertThat(failed.getError()).contains("Invalid or unreadable Open Library ratings dump");
		assertThat(mappingRepository.findByIndexVersion(started.getStagingVersion())).isEmpty();
		assertThat(ratingRepository.findByIndexVersionAndWorkId(started.getStagingVersion(), "OL1W")).isEmpty();
		assertThat(versionRepository.findById(started.getStagingVersion())).isEmpty();
		assertThat(versionRepository.findById("old-version")).get()
				.extracting(OpenLibraryIndexVersionMongoEntity::isActive).isEqualTo(true);

		ratingsFixture = gzip("repaired-ratings-fixture.txt.gz", """
				Work Key\tEdition Key\tRating\tDate
				/works/OL1W\t/books/OL1M\t4\t2026-08-01
				""");
		indexManager.resume();
		OpenLibraryIndexJobMongoEntity resumed = awaitTerminalStatus();

		assertThat(resumed.getStatus()).isEqualTo(OpenLibraryIndexJobStatus.COMPLETED);
		assertThat(resumed.getActiveVersion()).isEqualTo(started.getStagingVersion());
		assertThat(ratingRepository.findByIndexVersionAndWorkId(resumed.getActiveVersion(), "OL1W")).isPresent();
	}

	@Test
	void cancelsTheRunningJobAndCleansItsStagingData() throws Exception {
		CountDownLatch downloadStarted = new CountDownLatch(1);
		when(downloadPort.download(anyString(), any(Path.class), any(), any())).thenAnswer(invocation -> {
			BooleanSupplier cancelled = invocation.getArgument(2);
			downloadStarted.countDown();
			while (!cancelled.getAsBoolean()) {
				Thread.sleep(5L);
			}
			throw new OpenLibraryIndexCancelledException();
		});

		OpenLibraryIndexJobMongoEntity started = indexManager.start();
		assertThat(downloadStarted.await(5, TimeUnit.SECONDS)).isTrue();
		indexManager.cancel();
		OpenLibraryIndexJobMongoEntity cancelled = awaitTerminalStatus();

		assertThat(cancelled.getStatus()).isEqualTo(OpenLibraryIndexJobStatus.CANCELLED);
		assertThat(mappingRepository.findByIndexVersion(started.getStagingVersion())).isEmpty();
		assertThat(versionRepository.findById(started.getStagingVersion())).isEmpty();
		assertThat(Path.of(cancelled.getEditionsFile())).doesNotExist();
		assertThat(Path.of(cancelled.getRatingsFile())).doesNotExist();
	}

	@Test
	void resumesAPersistedRunningJobAfterStartup() throws Exception {
		String version = "interrupted-version";
		Path downloads = temporaryDirectory.resolve("index/downloads").resolve(version);
		Date now = new Date();
		jobRepository.save(OpenLibraryIndexJobMongoEntity.builder()
				.id(OpenLibraryIndexManager.JOB_ID)
				.status(OpenLibraryIndexJobStatus.DOWNLOADING_EDITIONS)
				.stagingVersion(version)
				.editionsUrl("https://fixture/editions")
				.ratingsUrl("https://fixture/ratings")
				.editionsFile(downloads.resolve("editions.txt.gz").toString())
				.ratingsFile(downloads.resolve("ratings.txt.gz").toString())
				.requestedAt(now)
				.startedAt(now)
				.updatedAt(now)
				.build());

		indexManager.resumeInterruptedJob();
		OpenLibraryIndexJobMongoEntity completed = awaitTerminalStatus();

		assertThat(completed.getStatus()).isEqualTo(OpenLibraryIndexJobStatus.COMPLETED);
		assertThat(completed.getActiveVersion()).isEqualTo(version);
	}

	@Test
	void failsBeforeDownloadingWhenThereIsNotEnoughFreeSpace() throws Exception {
		ReflectionTestUtils.setField(indexManager, "minimumFreeSpaceBytes", Long.MAX_VALUE);

		indexManager.start();
		OpenLibraryIndexJobMongoEntity failed = awaitTerminalStatus();

		assertThat(failed.getStatus()).isEqualTo(OpenLibraryIndexJobStatus.FAILED);
		assertThat(failed.getError()).contains("Insufficient free space");
	}

	private OpenLibraryIndexJobMongoEntity awaitTerminalStatus() throws InterruptedException {
		final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
		OpenLibraryIndexJobMongoEntity job;
		do {
			job = indexManager.status();
			if (!indexManager.isExecuting() && (job.getStatus() == OpenLibraryIndexJobStatus.COMPLETED
					|| job.getStatus() == OpenLibraryIndexJobStatus.FAILED
					|| job.getStatus() == OpenLibraryIndexJobStatus.CANCELLED)) {
				return job;
			}
			Thread.sleep(25L);
		}
		while (System.nanoTime() < deadline);
		throw new AssertionError("Open Library index job did not finish; last status: " + job.getStatus());
	}

	private Path gzip(final String name, final String contents) throws IOException {
		Path target = temporaryDirectory.resolve(name);
		try (GZIPOutputStream output = new GZIPOutputStream(Files.newOutputStream(target))) {
			output.write(contents.getBytes(StandardCharsets.UTF_8));
		}
		return target;
	}

	private void cleanDatabase() {
		jobRepository.deleteAll();
		versionRepository.deleteAll();
		mappingRepository.deleteAll();
		ratingRepository.deleteAll();
		bookRepository.deleteAll();
	}
}
