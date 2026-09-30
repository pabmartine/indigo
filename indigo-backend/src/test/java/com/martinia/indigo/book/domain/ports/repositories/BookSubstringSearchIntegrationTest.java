package com.martinia.indigo.book.domain.ports.repositories;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.domain.model.Search;
import com.martinia.indigo.configuration.infrastructure.mongo.BookSearchIndex;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BookSubstringSearchIntegrationTest extends BaseIndigoTest {
    @Autowired MongoTemplate mongo;
    @Autowired BookSearchIndex index;

    private BookMongoEntity book(String title, String... authors) {
        return bookRepository.save(BookMongoEntity.builder().title(title).authors(List.of(authors))
                .path("/unrelated-folder/" + UUID.randomUUID()).languages(List.of("es")).pages(123).tags(List.of("Novela")).build());
    }

    private List<String> titles(Search search) {
        return bookRepository.findSummaryPage(search, 0, 20, "id", "desc").items().stream().map(b -> b.getTitle()).toList();
    }

    @Test void globalSearchUsesRealTitlesAndEveryAuthorWithLiteralSubstringSemantics() {
        book("Cien años de soledad", "Gabriel García Márquez");
        book("Otra novela", "Primero", "Gabriel García Márquez");
        book("Unrelated", "Nobody");
        var search = new Search();
        search.setPath("arcia mar");
        assertEquals(2, titles(search).size());
        search.setPath("ÑOS DE SOLE");
        assertEquals(List.of("Cien años de soledad"), titles(search));
        search.setPath("unrelated-folder");
        assertTrue(titles(search).isEmpty(), "A folder name is not a title or author");
        book("Literal [a+b].*", "Special");
        search.setPath("[a+b].*");
        assertEquals(List.of("Literal [a+b].*"), titles(search));
        search.setPath(".*");
        assertEquals(List.of("Literal [a+b].*"), titles(search));
    }

    @Test void advancedSearchCombinesFieldsAndOtherFiltersAndKeepsTotalsWhenPaging() {
        for (int i = 0; i < 5; i++) book("El invierno " + i, "Ana María López");
        book("El invierno ajeno", "Pedro");
        var english = book("El invierno inglés", "Ana María López");
        english.setLanguages(List.of("en")); bookRepository.save(english);
        var search = new Search();
        search.setTitle("vierno"); search.setAuthor("maria lop");
        search.setLanguages(List.of("spa")); search.setSelectedTags(List.of("Novela"));
        search.setMin(120); search.setMax(130);
        var page = bookRepository.findSummaryPage(search, 1, 2, "title", "asc");
        assertEquals(5, page.total());
        assertEquals(List.of("El invierno 2", "El invierno 3"), page.items().stream().map(b -> b.getTitle()).toList());
        assertEquals(5, bookRepository.countBooks(search));
        search.setMax(122); assertTrue(titles(search).isEmpty());
    }

    @Test void gramsNeverProduceFalsePositivesAcrossFragmentsOrDifferentAuthors() {
        book("abc bcd", "abc", "bcd");
        book("abcd", "abcd");
        var search = new Search(); search.setTitle("abcd");
        assertEquals(List.of("abcd"), titles(search));
        search.setTitle(null); search.setAuthor("abcd");
        assertEquals(List.of("abcd"), titles(search));
        for (String shortTerm : List.of("a", "bc", "bcd")) {
            search.setAuthor(shortTerm); assertEquals(2, titles(search).size());
        }
        book("Astral 🪐 book", "Astronaut");
        search.setAuthor(null); search.setTitle("🪐");
        assertEquals(List.of("Astral 🪐 book"), titles(search));
    }

    @Test void saveUpdatesIndexAndMigrationIsResumableWithoutTouchingBookContent() {
        var saved = book("Before", "Old Author");
        saved.setTitle("After"); saved.setAuthors(List.of("New Author")); bookRepository.save(saved);
        var search = new Search(); search.setPath("Before"); assertTrue(titles(search).isEmpty());
        search.setPath("new author"); assertEquals(List.of("After"), titles(search));
        ObjectId legacyId = new ObjectId();
        mongo.getCollection("books").insertOne(new Document("_id", legacyId).append("title", "Legacy title")
                .append("authors", List.of("Legacy Author")).append("image", "keep-cover").append("comment", "keep-description"));
        index.backfill(); index.backfill();
        search.setPath("gacy"); assertEquals(List.of("Legacy title"), titles(search));
        var stored = mongo.getCollection("books").find(new Document("_id", legacyId)).first();
        assertEquals("keep-cover", stored.getString("image"));
        assertEquals("keep-description", stored.getString("comment"));
        assertNotNull(stored.get("search"));
        bookRepository.delete(saved);
        search.setPath("new author"); assertTrue(titles(search).isEmpty());
    }

    @Test void selectiveSubstringUsesIndexInsteadOfScanningTheCatalog() {
        for (int i = 0; i < 500; i++) book("Ordinary title " + i, "Ordinary author");
        book("The needlexyz title", "Rare person");
        Document criterion = BookSearchIndex.global("needlexyz").getCriteriaObject();
        Document stats = explain(criterion, new Document("_id", -1), 20, BookSearchIndex.INDEX);
        assertTrue(stats.toJson().contains("IXSCAN"), stats.toJson());
        assertTrue(stats.toJson().contains(BookSearchIndex.INDEX), stats.toJson());
        Number examined = stats.get("executionStats", Document.class).get("totalDocsExamined", Number.class);
        assertTrue(examined.longValue() < 10, stats.toJson());
    }

    private Document explain(Document filter, Document sort, int limit, String hint) {
        Document find = new Document("find", "books").append("filter", filter).append("sort", sort)
                .append("projection", new Document("title", 1).append("authors", 1)).append("limit", limit);
        if (hint != null) find.append("hint", hint);
        return mongo.executeCommand(new Document("explain", find).append("verbosity", "executionStats"));
    }

    @Test
    @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "search.benchmark.size", matches = "[1-9][0-9]*")
    void benchmarkSubstringQueries() {
        int size = Integer.getInteger("search.benchmark.size");
        var collection = mongo.getCollection("books");
        for (int start = 0; start < size; start += 500) {
            List<Document> batch = new ArrayList<>();
            for (int i = start; i < Math.min(size, start + 500); i++) {
                String title = i < 10 ? "La aguja extraordinaria " + i : "Historia general " + i;
                String author = i < 10 ? "Gabriel García Márquez" : "Autor común " + (i % 1000);
                batch.add(new Document("title", title).append("authors", List.of(author)).append("image", "x".repeat(1024))
                        .append("search", BookSearchIndex.fields(title, List.of(author))));
            }
            collection.insertMany(batch);
        }
        for (String kind : List.of("title", "author", "global")) {
            Document oldFilter = kind.equals("title") ? new Document("title", new Document("$regex", "extraordinaria").append("$options", "i"))
                    : kind.equals("author") ? new Document("authors", new Document("$regex", "Márquez").append("$options", "i"))
                    : new Document("$or", List.of(new Document("title", new Document("$regex", "Márquez").append("$options", "i")),
                            new Document("authors", new Document("$regex", "Márquez").append("$options", "i"))));
            Document optimized = (kind.equals("title") ? BookSearchIndex.title("extraordinaria")
                    : kind.equals("author") ? BookSearchIndex.author("Márquez") : BookSearchIndex.global("Márquez")).getCriteriaObject();
            Document oldStats = explain(oldFilter, new Document("_id", -1), 20, null).get("executionStats", Document.class);
            Document newStats = explain(optimized, new Document("_id", -1), 20, BookSearchIndex.INDEX).get("executionStats", Document.class);
            System.out.printf("SEARCH BENCHMARK size=%d kind=%s beforeMs=%s afterMs=%s beforeDocs=%s afterDocs=%s%n", size, kind,
                    oldStats.get("executionTimeMillis"), newStats.get("executionTimeMillis"), oldStats.get("totalDocsExamined"), newStats.get("totalDocsExamined"));
            assertEquals(oldStats.get("nReturned"), newStats.get("nReturned"));
            assertTrue(((Number)newStats.get("totalDocsExamined")).intValue() < 100);
        }
        var stats = collection.aggregate(List.of(new Document("$collStats", new Document("storageStats", new Document("scale", 1))))).first();
        var storage = stats.get("storageStats", Document.class);
        System.out.println("SEARCH BENCHMARK indexSizes=" + storage.get("indexSizes") + " dataBytes=" + storage.get("size"));
    }
}
