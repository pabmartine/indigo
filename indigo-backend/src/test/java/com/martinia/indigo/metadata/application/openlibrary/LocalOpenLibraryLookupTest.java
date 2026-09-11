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
        when(mappings.findFirstByIndexVersionAndIsbnIn(eq("active"), any())).thenReturn(Optional.of(OpenLibraryEditionMappingMongoEntity.builder().editionId("OL1M").workId("OL1W").build()));
        when(ratings.findByIndexVersionAndWorkId("active", "OL1W")).thenReturn(Optional.of(OpenLibraryRatingMongoEntity.builder().average(4.5F).count(20).build()));
        var result = lookup.find(BookMetadataQuery.builder().isbn13(List.of("9780306406157")).build());
        assertEquals(4.5F, result.getRatingAverage());
        assertEquals(20, result.getRatingsCount());
        assertEquals("OL1W", result.getOpenLibraryWorkId());
        verify(ratings, never()).findByIndexVersionAndWorkId(eq("pending"), any());
    }
}
