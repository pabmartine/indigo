package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.metadata.domain.model.*;
import com.martinia.indigo.metadata.domain.ports.adapters.google.FindGoogleBooksBookPort;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryBookPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import java.util.concurrent.CancellationException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookMetadataFailureTest {
    private final BookRepository repository = mock(BookRepository.class);
    private final FindGoogleBooksBookPort google = mock(FindGoogleBooksBookPort.class);
    private final FindOpenLibraryBookPort library = mock(FindOpenLibraryBookPort.class);
    private final FindBookMetadataUseCaseImpl useCase = new FindBookMetadataUseCaseImpl();
    private final BookMongoEntity book = BookMongoEntity.builder().id("book").title("Book")
            .isbn13(List.of("9780306406157")).build();

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(useCase, "bookRepository", repository);
        ReflectionTestUtils.setField(useCase, "findGoogleBooksBookPort", Optional.of(google));
        ReflectionTestUtils.setField(useCase, "findOpenLibraryBookPort", Optional.of(library));
        when(repository.findById("book")).thenReturn(Optional.of(book));
    }
    private MetadataItemResult find() {
        return useCase.find("book", MetadataMergePolicy.FILL_MISSING, DynamicMetadataPolicy.REFRESH_IF_STALE, 0);
    }
    @Test void emptyOpenLibraryAndFailedGoogleDoNotPersistFalseNoMatch() {
        when(google.findBook(any())).thenThrow(new IllegalStateException("paused"));
        assertThat(find()).isEqualTo(MetadataItemResult.ERROR);
        assertThat(book.getMetadataMatchStatus()).isNull();
        assertThat(book.getLastMetadataSync()).isNull();
        verify(repository, never()).updateBookMetadata(any());
    }
    @Test void failedOpenLibraryAndEmptyGoogleAlsoRemainPending() {
        when(library.findBook(any())).thenThrow(new IllegalStateException("unavailable"));
        assertThat(find()).isEqualTo(MetadataItemResult.ERROR);
        verify(repository, never()).updateBookMetadata(any());
    }
    @Test void partialIdentityIsSavedButMissingRatingRemainsPendingAfterFailure() {
        when(library.findBook(any())).thenReturn(BookMetadataResult.builder().provider("OPEN_LIBRARY")
                .openLibraryWorkId("OL1W").build());
        when(google.findBook(any())).thenThrow(new IllegalStateException("unavailable"));
        assertThat(find()).isEqualTo(MetadataItemResult.ERROR);
        assertThat(book.getOpenLibraryWorkId()).isEqualTo("OL1W");
        assertThat(book.getLastMetadataSync()).isNull();
        assertThat(book.getRatingUpdatedAt()).isNull();
        verify(repository).updateBookMetadata(book);
    }
    @Test void validFallbackRatingCompletesDespiteFirstProviderFailure() {
        when(library.findBook(any())).thenThrow(new IllegalStateException("unavailable"));
        when(google.findBook(any())).thenReturn(BookMetadataResult.builder().provider("GOOGLE")
                .ratingAverage(4F).build());
        assertThat(find()).isEqualTo(MetadataItemResult.FOUND);
        assertThat(book.getRatingAverage()).isEqualTo(4F);
        verify(repository).updateBookMetadata(book);
    }
    @Test void genuineEmptyResponsesAreNoMatch() {
        assertThat(find()).isEqualTo(MetadataItemResult.NOT_FOUND);
        assertThat(book.getMetadataMatchStatus()).isEqualTo("NO_MATCH");
        verify(repository).updateBookMetadata(book);
    }
    @Test void previousNoMatchCanBeRetried() {
        book.setMetadataMatchStatus("NO_MATCH");
        assertThat(find()).isEqualTo(MetadataItemResult.NOT_FOUND);
        verify(library).findBook(any());
        verify(google).findBook(any());
    }
    @Test void cancellationDoesNotFallThroughToNextProviderOrSave() {
        when(library.findBook(any())).thenThrow(new IllegalStateException(new CancellationException()));
        assertThatThrownBy(this::find).isInstanceOf(CancellationException.class);
        verifyNoInteractions(google);
        verify(repository, never()).updateBookMetadata(any());
    }
    @Test void missingRatingIsNotSkippedEvenIfTimestampIsRecent() {
        book.setRatingUpdatedAt(new Date());
        assertThat(find()).isEqualTo(MetadataItemResult.NOT_FOUND);
        verify(library).findBook(any());
    }
    @Test void isbnSelectionSkipsBlankEntriesAndNormalizesSeparators() {
        assertThat(BookMetadataQuery.builder().isbn13(Arrays.asList(null, " ", "978-0-306-40615-7"))
                .build().preferredIsbn()).isEqualTo("9780306406157");
        assertThat(BookMetadataQuery.builder().isbn13(List.of(" ")).isbn10(List.of("0 306 40615 2"))
                .build().preferredIsbn()).isEqualTo("0306406152");
    }
    @Test void stoppedRunCannotSaveResultOfRequestAlreadyInFlight() {
        var active = new java.util.concurrent.atomic.AtomicBoolean(true);
        when(library.findBook(any())).thenAnswer(ignored -> {
            active.set(false);
            return BookMetadataResult.builder().ratingAverage(4F).build();
        });
        assertThatThrownBy(() -> com.martinia.indigo.common.util.BookProviderHttpRequests.whileActive(active::get, this::find))
                .isInstanceOf(CancellationException.class);
        verify(repository, never()).updateBookMetadata(any());
    }
}
