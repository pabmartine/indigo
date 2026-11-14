package com.martinia.indigo.metadata.application.openlibrary;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.util.BookProviderHttpRequests;
import com.martinia.indigo.file.application.EpubImportPolicy;
import com.martinia.indigo.metadata.application.ProviderDiagnostics;
import com.martinia.indigo.metadata.domain.model.BookMetadataQuery;
import com.martinia.indigo.metadata.domain.model.BookMetadataResult;
import com.martinia.indigo.metadata.domain.ports.repositories.*;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LocalOpenLibraryLookup {
    @Resource private OpenLibraryIndexJobRepository jobs;
    @Resource private OpenLibraryEditionMappingRepository mappings;
    @Resource private OpenLibraryRatingRepository ratings;
    @Resource private BookRepository books;

    public BookMetadataResult find(BookMetadataQuery query) {
        var job = jobs.findById(OpenLibraryIndexManager.JOB_ID).orElse(null);
        if (job == null || job.getActiveVersion() == null) {
            trace("NOT_FOUND", "No hay un índice local de libros activo; el índice de autores no garantiza cobertura de libros");
            return null;
        }
        String version = job.getActiveVersion();
        if (validWorkId(query.getOpenLibraryWorkId())) {
            var rating = ratings.findByIndexVersionAndWorkId(version, query.getOpenLibraryWorkId()).orElse(null);
            if (valid(rating)) return result(query.getOpenLibraryWorkId(), null, rating);
        }
        var isbns = EpubImportPolicy.isbnKeys(query.getIsbn10(), query.getIsbn13());
        List<OpenLibraryEditionMappingMongoEntity> candidates = isbns.isEmpty() ? List.of()
                : mappings.findByIndexVersionAndIsbnIn(version, isbns);
        if (candidates.isEmpty()) {
            var keys = LocalBookKey.keys(query.getTitle(), query.getAuthors());
            if (!keys.isEmpty()) candidates = mappings.findByIndexVersionAndTitleAuthorKeysIn(version, keys);
        }
        var mapping = unambiguous(candidates);
        if (mapping == null) {
            trace("NOT_FOUND", !candidates.isEmpty() ? "Coincidencia local ambigua; no se asigna una valoración"
                    : job.getBookLookupVersion() < 2
                    ? "Sin coincidencia por ISBN; este índice antiguo necesita reconstruirse para buscar por título y autor"
                    : "El índice local no contiene una coincidencia por ISBN o título y autor");
            return null;
        }
        return result(mapping.getWorkId(), mapping.getEditionId(),
                ratings.findByIndexVersionAndWorkId(version, mapping.getWorkId()).orElse(null));
    }

    /** Load the small filtered index once and query matching candidate books directly, or fallback to batch scan. */
    public Set<String> ratedBookIds(Set<String> pending) {
        var job = jobs.findById(OpenLibraryIndexManager.JOB_ID).orElse(null);
        if (job == null || job.getActiveVersion() == null || pending.isEmpty()) return Set.of();
        Set<String> ratedWorks = new HashSet<>();
        for (var rating : ratings.findByIndexVersion(job.getActiveVersion()))
            if (valid(rating)) ratedWorks.add(rating.getWorkId());
        if (ratedWorks.isEmpty()) return Set.of();
        Map<String, List<OpenLibraryEditionMappingMongoEntity>> byIsbn = new HashMap<>(), byTitle = new HashMap<>();
        Set<String> relevantIsbns = new HashSet<>();
        Set<String> relevantTitles = new HashSet<>();
        for (var mapping : mappings.findByIndexVersion(job.getActiveVersion())) {
            BookProviderHttpRequests.checkActive();
            if (mapping.getIsbn() != null) byIsbn.computeIfAbsent(mapping.getIsbn(), key -> new ArrayList<>()).add(mapping);
            if (mapping.getTitleAuthorKeys() != null) for (String key : mapping.getTitleAuthorKeys()) {
                byTitle.computeIfAbsent(key, ignored -> new ArrayList<>()).add(mapping);
                if (validWorkId(mapping.getWorkId()) && ratedWorks.contains(mapping.getWorkId())) {
                    int pipe = key.indexOf('|');
                    relevantTitles.add(pipe > 0 ? key.substring(0, pipe) : key);
                }
            }
            if (mapping.getIsbn() != null && validWorkId(mapping.getWorkId()) && ratedWorks.contains(mapping.getWorkId())) {
                relevantIsbns.add(mapping.getIsbn());
            }
        }

        // When the set of targets is small (typical in practice), query candidates directly instead of scanning the full collection.
        if (ratedWorks.size() <= 500 && relevantIsbns.size() <= 500 && relevantTitles.size() <= 500) {
            Set<String> candidateIds = new HashSet<>();
            List<BookMongoEntity> candidateBooks = new ArrayList<>();
            for (var book : books.findCandidatesByOpenLibraryWorkIdIn(ratedWorks)) {
                if (candidateIds.add(book.getId())) candidateBooks.add(book);
            }
            if (!relevantIsbns.isEmpty()) {
                for (var book : books.findCandidatesByAnyIsbn(relevantIsbns)) {
                    if (candidateIds.add(book.getId())) candidateBooks.add(book);
                }
            }
            if (!relevantTitles.isEmpty()) {
                for (var book : books.findCandidatesByTitleIn(relevantTitles)) {
                    if (candidateIds.add(book.getId())) candidateBooks.add(book);
                }
            }
            Set<String> found = new HashSet<>();
            for (var book : candidateBooks) {
                if (pending.contains(book.getId()) && matchesRated(book, ratedWorks, byIsbn, byTitle)) {
                    found.add(book.getId());
                }
            }
            return found;
        }

        Set<String> found = new HashSet<>();
        String after = null;
        while (true) {
            BookProviderHttpRequests.checkActive();
            var batch = books.findMetadataIndexBatch(after);
            if (batch.isEmpty()) return found;
            for (var book : batch) {
                if (pending.contains(book.getId()) && matchesRated(book, ratedWorks, byIsbn, byTitle)) {
                    found.add(book.getId());
                }
            }
            after = batch.getLast().getId();
        }
    }

    private boolean matchesRated(BookMongoEntity book, Set<String> ratedWorks,
                                 Map<String, List<OpenLibraryEditionMappingMongoEntity>> byIsbn,
                                 Map<String, List<OpenLibraryEditionMappingMongoEntity>> byTitle) {
        if (validWorkId(book.getOpenLibraryWorkId()) && ratedWorks.contains(book.getOpenLibraryWorkId())) {
            return true;
        }
        List<OpenLibraryEditionMappingMongoEntity> candidates = new ArrayList<>();
        for (String isbn : EpubImportPolicy.isbnKeys(book.getIsbn10(), book.getIsbn13()))
            candidates.addAll(byIsbn.getOrDefault(isbn, List.of()));
        if (candidates.isEmpty()) for (String key : LocalBookKey.keys(book.getTitle(), book.getAuthors()))
            candidates.addAll(byTitle.getOrDefault(key, List.of()));
        var mapping = unambiguous(candidates);
        return mapping != null && ratedWorks.contains(mapping.getWorkId());
    }

    private OpenLibraryEditionMappingMongoEntity unambiguous(List<OpenLibraryEditionMappingMongoEntity> candidates) {
        var valid = candidates.stream().filter(mapping -> validWorkId(mapping.getWorkId())).toList();
        if (valid.isEmpty() || valid.stream().map(OpenLibraryEditionMappingMongoEntity::getWorkId).distinct().count() != 1) return null;
        return valid.getFirst();
    }

    private boolean validWorkId(String id) { return id != null && id.matches("OL[0-9]+W"); }

    private boolean valid(OpenLibraryRatingMongoEntity rating) {
        return rating != null && rating.getAverage() != null && Float.isFinite(rating.getAverage())
                && rating.getAverage() >= 1 && rating.getAverage() <= 5 && rating.getCount() > 0;
    }

    private BookMetadataResult result(String work, String edition, OpenLibraryRatingMongoEntity rating) {
        var result = BookMetadataResult.builder().provider("OPEN_LIBRARY").openLibraryWorkId(work)
                .openLibraryEditionId(edition).matchConfidence(1D);
        if (valid(rating)) result.ratingAverage(rating.getAverage()).ratingsCount(rating.getCount()).ratingDistribution(rating.getDistribution());
        trace("FOUND", valid(rating) ? "Valoración obtenida del índice local, sin peticiones externas"
                : "Libro identificado en el índice local, pero sin valoraciones en el volcado");
        return result.build();
    }

    private void trace(String status, String message) {
        ProviderDiagnostics.event("OPEN_LIBRARY_LOCAL", "Consultar índice de libros", status, message);
    }
}
