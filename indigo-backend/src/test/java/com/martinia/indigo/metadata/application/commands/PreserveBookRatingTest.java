package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.metadata.domain.model.*;
import com.martinia.indigo.metadata.domain.ports.adapters.google.FindGoogleBooksBookPort;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PreserveBookRatingTest {
    @Test
    void identityWithoutRatingPreservesPreviousRating() {
        var repository = mock(BookRepository.class);
        var google = mock(FindGoogleBooksBookPort.class);
        var useCase = new FindBookMetadataUseCaseImpl();
        ReflectionTestUtils.setField(useCase, "bookRepository", repository);
        ReflectionTestUtils.setField(useCase, "eventBus", mock(EventBus.class));
        ReflectionTestUtils.setField(useCase, "findGoogleBooksBookPort", Optional.of(google));
        ReflectionTestUtils.setField(useCase, "findOpenLibraryBookPort", Optional.empty());
        var book = BookMongoEntity.builder().id("book").title("Book").rating(4F).ratingAverage(4F).build();
        when(repository.findById("book")).thenReturn(Optional.of(book));
        when(google.findBook(any())).thenReturn(BookMetadataResult.builder().build());
        assertEquals(MetadataItemResult.FOUND, useCase.find("book", MetadataMergePolicy.FILL_MISSING,
                DynamicMetadataPolicy.REFRESH_IF_STALE, 0));
        assertEquals(4F, book.getRating());
        assertEquals(4F, book.getRatingAverage());
        assertNull(book.getRatingUpdatedAt());
    }
}
