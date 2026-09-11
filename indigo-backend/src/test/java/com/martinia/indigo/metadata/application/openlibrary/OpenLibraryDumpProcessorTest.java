package com.martinia.indigo.metadata.application.openlibrary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import com.martinia.indigo.metadata.domain.model.OpenLibraryIndexCancelledException;
import com.martinia.indigo.metadata.domain.ports.repositories.OpenLibraryEditionMappingRepository;
import com.martinia.indigo.metadata.domain.ports.repositories.OpenLibraryRatingRepository;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryEditionMappingMongoEntity;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryRatingMongoEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OpenLibraryDumpProcessorTest {
	@TempDir
	private Path temporaryDirectory;

	@Mock
	private OpenLibraryEditionMappingRepository mappingRepository;
	@Mock
	private OpenLibraryRatingRepository ratingRepository;
	@InjectMocks
	private OpenLibraryDumpProcessor processor;

	@Test
	void buildsAFilteredVersionedIndexFromCompressedDumps() throws IOException {
		Path editionsDump = gzip("editions.txt.gz", """
				/type/edition\t/books/OL1M\t1\t2026-08-01T00:00:00\t{"key":"/books/OL1M","isbn_13":["978-0-261-10221-7"],"works":[{"key":"/works/OL1W"}]}
				/type/edition\t/books/OL2M\t1\t2026-08-01T00:00:00\t{"key":"/books/OL2M","isbn_10":["0261102214"],"works":[{"key":"/works/OL1W"}]}
				/type/edition\t/books/OL3M\t1\t2026-08-01T00:00:00\t{"key":"/books/OL3M","isbn_13":["9780000000002"],"works":[{"key":"/works/OL3W"}]}
				""");

		OpenLibraryDumpProcessor.EditionProcessingResult editions = processor.processEditions(editionsDump,
				"version-1", Set.of("9780261102217", "0261102214"), () -> false, ignored -> {
				});

		assertThat(editions.processedRecords()).isEqualTo(3);
		assertThat(editions.matchedIsbns()).containsExactlyInAnyOrder("9780261102217", "0261102214");
		assertThat(editions.matchedWorks()).containsExactly("OL1W");
		ArgumentCaptor<List<OpenLibraryEditionMappingMongoEntity>> mappings = ArgumentCaptor.forClass(List.class);
		verify(mappingRepository).saveAll(mappings.capture());
		assertThat(mappings.getValue()).extracting(OpenLibraryEditionMappingMongoEntity::getEditionId)
				.containsExactlyInAnyOrder("OL1M", "OL2M");

		Path ratingsDump = gzip("ratings.txt.gz", """
				Work Key\tEdition Key\tRating\tDate
				/works/OL1W\t/books/OL1M\t4\t2026-08-01
				/works/OL1W\t\t5\t2026-08-02
				/works/OL3W\t/books/OL3M\t1\t2026-08-03
				""");

		OpenLibraryDumpProcessor.RatingProcessingResult ratings = processor.processRatings(ratingsDump,
				"version-1", editions.matchedWorks(), () -> false, ignored -> {
				});

		assertThat(ratings.processedRecords()).isEqualTo(4);
		assertThat(ratings.worksWithRatings()).isEqualTo(1);
		ArgumentCaptor<List<OpenLibraryRatingMongoEntity>> savedRatings = ArgumentCaptor.forClass(List.class);
		verify(ratingRepository).saveAll(savedRatings.capture());
		OpenLibraryRatingMongoEntity rating = savedRatings.getValue().get(0);
		assertThat(rating.getWorkId()).isEqualTo("OL1W");
		assertThat(rating.getAverage()).isEqualTo(4.5F);
		assertThat(rating.getCount()).isEqualTo(2);
		assertThat(rating.getDistribution()).containsEntry("4", 1L).containsEntry("5", 1L);
	}

	@Test
	void rejectsACorruptGzipWithoutPublishingRecords() throws IOException {
		Path corruptDump = temporaryDirectory.resolve("corrupt.txt.gz");
		Files.writeString(corruptDump, "not gzip", StandardCharsets.UTF_8);

		assertThatThrownBy(() -> processor.processEditions(corruptDump, "version-2", Set.of("9780261102217"),
				() -> false, ignored -> {
				})).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Invalid or unreadable");
	}

	@Test
	void honoursCancellationWhileStreaming() throws IOException {
		Path dump = gzip("cancel.txt.gz", """
				/type/edition\t/books/OL1M\t1\t2026-08-01T00:00:00\t{"key":"/books/OL1M"}
				""");

		assertThatThrownBy(() -> processor.processEditions(dump, "version-3", Set.of(), () -> true,
				ignored -> {
				})).isInstanceOf(OpenLibraryIndexCancelledException.class);
	}

	private Path gzip(final String name, final String contents) throws IOException {
		Path target = temporaryDirectory.resolve(name);
		try (GZIPOutputStream output = new GZIPOutputStream(Files.newOutputStream(target))) {
			output.write(contents.getBytes(StandardCharsets.UTF_8));
		}
		return target;
	}
}
