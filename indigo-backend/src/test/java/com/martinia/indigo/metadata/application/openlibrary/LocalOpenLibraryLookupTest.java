package com.martinia.indigo.metadata.application.openlibrary;

import com.martinia.indigo.metadata.domain.model.BookMetadataQuery;
import com.martinia.indigo.metadata.domain.ports.repositories.*;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class LocalOpenLibraryLookupTest {
    @Test void usesOnlyTheCanonicalActiveVersion() {
        var jobs = mock(OpenLibraryIndexJobRepository.class);
        var mappings = mock(OpenLibraryEditionMappingRepository.class);
        var ratings = mock(OpenLibraryRatingRepository.class);
        var lookup = new LocalOpenLibraryLookup();
        ReflectionTestUtils.setField(lookup, "jobs", jobs);
        ReflectionTestUtils.setField(lookup, "mappings", mappings);
        ReflectionTestUtils.setField(lookup, "ratings", ratings);
        when(jobs.findById(OpenLibraryIndexManager.JOB_ID)).thenReturn(Optional.of(OpenLibraryIndexJobMongoEntity.builder().activeVersion("active").stagingVersion("pending").build()));
        when(mappings.findByIndexVersionAndIsbnIn(eq("active"), any())).thenReturn(List.of(OpenLibraryEditionMappingMongoEntity.builder().editionId("OL1M").workId("OL1W").build()));
        when(ratings.findByIndexVersionAndWorkId("active", "OL1W")).thenReturn(Optional.of(OpenLibraryRatingMongoEntity.builder().average(4.5F).count(20).build()));
        var result = lookup.find(BookMetadataQuery.builder().isbn13(List.of("9780306406157")).build());
        assertEquals(4.5F, result.getRatingAverage());
        assertEquals(20, result.getRatingsCount());
        assertEquals("OL1W", result.getOpenLibraryWorkId());
        verify(ratings, never()).findByIndexVersionAndWorkId(eq("pending"), any());
        when(ratings.findByIndexVersionAndWorkId("active", "OL1W")).thenReturn(Optional.empty());
        var identity = lookup.find(BookMetadataQuery.builder().isbn13(List.of("9780306406157")).build());
        assertEquals("OL1W", identity.getOpenLibraryWorkId());
        assertNull(identity.getRatingAverage());
    }
    @Test void titleAndAuthorResolveAnUnambiguousWorkWithoutIsbn() {
        var jobs = mock(OpenLibraryIndexJobRepository.class);
        var mappings = mock(OpenLibraryEditionMappingRepository.class);
        var ratings = mock(OpenLibraryRatingRepository.class);
        var lookup = new LocalOpenLibraryLookup();
        ReflectionTestUtils.setField(lookup, "jobs", jobs);
        ReflectionTestUtils.setField(lookup, "mappings", mappings);
        ReflectionTestUtils.setField(lookup, "ratings", ratings);
        when(jobs.findById(OpenLibraryIndexManager.JOB_ID)).thenReturn(Optional.of(
                OpenLibraryIndexJobMongoEntity.builder().activeVersion("v").bookLookupVersion(2).build()));
        var keys = LocalBookKey.keys("El Hobbit", List.of("J. R. R. Tolkien"));
        when(mappings.findByIndexVersionAndTitleAuthorKeysIn("v", keys)).thenReturn(List.of(
                OpenLibraryEditionMappingMongoEntity.builder().workId("OL1W").editionId("OL1M").build()));
        when(ratings.findByIndexVersionAndWorkId("v", "OL1W")).thenReturn(Optional.of(
                OpenLibraryRatingMongoEntity.builder().average(4.5F).count(20).build()));
        assertEquals(4.5F, lookup.find(BookMetadataQuery.builder().title("El Hobbit").authors(List.of("J. R. R. Tolkien")).build()).getRatingAverage());
        when(mappings.findByIndexVersionAndTitleAuthorKeysIn("v", keys)).thenReturn(List.of(
                OpenLibraryEditionMappingMongoEntity.builder().workId("OL1W").build(),
                OpenLibraryEditionMappingMongoEntity.builder().workId("OL2W").build()));
        assertNull(lookup.find(BookMetadataQuery.builder().title("El Hobbit").authors(List.of("J. R. R. Tolkien")).build()));
        assertNull(lookup.find(BookMetadataQuery.builder().title("El Hobbit").authors(List.of("Otro autor")).build()));
    }

    @Test void knownWorkCanRecoverRatingsEvenWithoutAnIsbnMapping() {
        var jobs = mock(OpenLibraryIndexJobRepository.class);
        var ratings = mock(OpenLibraryRatingRepository.class);
        var mappings = mock(OpenLibraryEditionMappingRepository.class);
        var lookup = new LocalOpenLibraryLookup();
        ReflectionTestUtils.setField(lookup, "jobs", jobs);
        ReflectionTestUtils.setField(lookup, "ratings", ratings);
        ReflectionTestUtils.setField(lookup, "mappings", mappings);
        when(jobs.findById(OpenLibraryIndexManager.JOB_ID)).thenReturn(Optional.of(OpenLibraryIndexJobMongoEntity.builder().activeVersion("v").build()));
        when(ratings.findByIndexVersionAndWorkId("v", "OL1W")).thenReturn(Optional.of(OpenLibraryRatingMongoEntity.builder().average(4F).count(2).build()));
        assertEquals(4F, lookup.find(BookMetadataQuery.builder().openLibraryWorkId("OL1W").build()).getRatingAverage());
        verifyNoInteractions(mappings);
    }

    @Test void ratedBookIdsUsesCandidateQueriesWhenTargetsAreFew() {
        var jobs = mock(OpenLibraryIndexJobRepository.class);
        var ratings = mock(OpenLibraryRatingRepository.class);
        var mappings = mock(OpenLibraryEditionMappingRepository.class);
        var books = mock(com.martinia.indigo.book.domain.ports.repositories.BookRepository.class);
        var lookup = new LocalOpenLibraryLookup();
        ReflectionTestUtils.setField(lookup, "jobs", jobs);
        ReflectionTestUtils.setField(lookup, "ratings", ratings);
        ReflectionTestUtils.setField(lookup, "mappings", mappings);
        ReflectionTestUtils.setField(lookup, "books", books);

        when(jobs.findById(OpenLibraryIndexManager.JOB_ID)).thenReturn(Optional.of(
                OpenLibraryIndexJobMongoEntity.builder().activeVersion("v").build()));
        when(ratings.findByIndexVersion("v")).thenReturn(List.of(
                OpenLibraryRatingMongoEntity.builder().workId("OL1W").average(4.2F).count(10).build()));
        when(mappings.findByIndexVersion("v")).thenReturn(List.of(
                OpenLibraryEditionMappingMongoEntity.builder().workId("OL1W").isbn("9780306406157").editionId("OL1M").build()));

        var book1 = com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity.builder()
                .id("b1").openLibraryWorkId("OL1W").build();
        var book2 = com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity.builder()
                .id("b2").isbn13(List.of("9780306406157")).build();

        when(books.findCandidatesByOpenLibraryWorkIdIn(Set.of("OL1W"))).thenReturn(List.of(book1));
        when(books.findCandidatesByAnyIsbn(Set.of("9780306406157"))).thenReturn(List.of(book2));

        Set<String> result = lookup.ratedBookIds(Set.of("b1", "b2", "b3"));
        assertEquals(Set.of("b1", "b2"), result);
        verify(books, never()).findMetadataIndexBatch(any());
    }

    @Test void ratedBookIdsFallsBackToBatchScanWhenManyRatedWorks() {
        var jobs = mock(OpenLibraryIndexJobRepository.class);
        var ratings = mock(OpenLibraryRatingRepository.class);
        var mappings = mock(OpenLibraryEditionMappingRepository.class);
        var books = mock(com.martinia.indigo.book.domain.ports.repositories.BookRepository.class);
        var lookup = new LocalOpenLibraryLookup();
        ReflectionTestUtils.setField(lookup, "jobs", jobs);
        ReflectionTestUtils.setField(lookup, "ratings", ratings);
        ReflectionTestUtils.setField(lookup, "mappings", mappings);
        ReflectionTestUtils.setField(lookup, "books", books);

        when(jobs.findById(OpenLibraryIndexManager.JOB_ID)).thenReturn(Optional.of(
                OpenLibraryIndexJobMongoEntity.builder().activeVersion("v").build()));

        List<OpenLibraryRatingMongoEntity> ratingList = new ArrayList<>();
        for (int i = 0; i < 501; i++) {
            ratingList.add(OpenLibraryRatingMongoEntity.builder().workId("OL" + i + "W").average(4F).count(5).build());
        }
        when(ratings.findByIndexVersion("v")).thenReturn(ratingList);
        when(mappings.findByIndexVersion("v")).thenReturn(List.of());

        var book1 = com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity.builder()
                .id("b1").openLibraryWorkId("OL1W").build();
        when(books.findMetadataIndexBatch(null)).thenReturn(List.of(book1));
        when(books.findMetadataIndexBatch("b1")).thenReturn(List.of());

        Set<String> result = lookup.ratedBookIds(Set.of("b1"));
        assertEquals(Set.of("b1"), result);
        verify(books).findMetadataIndexBatch(null);
        verify(books, never()).findCandidatesByOpenLibraryWorkIdIn(any());
    }

}
