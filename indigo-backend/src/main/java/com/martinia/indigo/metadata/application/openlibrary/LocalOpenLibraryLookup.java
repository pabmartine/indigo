package com.martinia.indigo.metadata.application.openlibrary;

import com.martinia.indigo.file.application.EpubImportPolicy;
import com.martinia.indigo.metadata.domain.model.BookMetadataQuery;
import com.martinia.indigo.metadata.domain.model.BookMetadataResult;
import com.martinia.indigo.metadata.domain.ports.repositories.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class LocalOpenLibraryLookup {
    @Resource private OpenLibraryIndexJobRepository jobs;
    @Resource private OpenLibraryEditionMappingRepository mappings;
    @Resource private OpenLibraryRatingRepository ratings;

    public BookMetadataResult find(BookMetadataQuery query) {
        var keys = EpubImportPolicy.isbnKeys(query.getIsbn10(), query.getIsbn13());
        if (keys.isEmpty()) return null;
        var job = jobs.findById(OpenLibraryIndexManager.JOB_ID).orElse(null);
        if (job == null || job.getActiveVersion() == null) return null;
        String version = job.getActiveVersion();
        var mapping = mappings.findFirstByIndexVersionAndIsbnIn(version, keys).orElse(null);
        if (mapping == null) return null;
        var rating = ratings.findByIndexVersionAndWorkId(version, mapping.getWorkId()).orElse(null);
        if (rating == null || rating.getAverage() == null || rating.getCount() <= 0) return null;
        return BookMetadataResult.builder().provider("OPEN_LIBRARY")
                .openLibraryWorkId(mapping.getWorkId()).openLibraryEditionId(mapping.getEditionId())
                .ratingAverage(rating.getAverage()).ratingsCount(rating.getCount())
                .ratingDistribution(rating.getDistribution()).matchConfidence(1D).build();
    }
}
