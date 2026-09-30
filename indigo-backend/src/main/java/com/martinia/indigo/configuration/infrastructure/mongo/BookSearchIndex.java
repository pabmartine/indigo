package com.martinia.indigo.configuration.infrastructure.mongo;

import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/** Indexed candidate selection followed by an exact substring check, without a separate search server. */
@Component
@Slf4j
public class BookSearchIndex implements BeforeSaveCallback<Object>, ApplicationRunner {
    public static final String INDEX = "book_substring_idx";
    private static final int VERSION = 1;
    private final MongoTemplate mongo;
    private volatile boolean ready;

    public BookSearchIndex(@org.springframework.context.annotation.Lazy MongoTemplate mongo) { this.mongo = mongo; }

    public void requireReady() {
        if (!ready) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "Preparando la búsqueda. Inténtalo de nuevo en unos instantes.");
    }

    public static String normalize(String text) {
        return StringUtils.stripAccents(StringUtils.defaultString(text)).toLowerCase(Locale.ROOT);
    }

    private static Set<String> grams(String text, int size) {
        int[] points = text.codePoints().toArray();
        Set<String> grams = new LinkedHashSet<>();
        for (int i = 0; i + size <= points.length; i++) grams.add(new String(points, i, size));
        return grams;
    }

    public static Document fields(String title, List<String> authors) {
        String normalizedTitle = normalize(title);
        List<String> normalizedAuthors = authors == null ? List.of() : authors.stream().map(BookSearchIndex::normalize).toList();
        Set<String> tokens = new LinkedHashSet<>();
        addTokens(tokens, "t:", normalizedTitle);
        normalizedAuthors.forEach(author -> addTokens(tokens, "a:", author));
        return new Document("version", VERSION).append("title", normalizedTitle)
                .append("authors", normalizedAuthors).append("grams", new ArrayList<>(tokens));
    }

    private static void addTokens(Set<String> tokens, String prefix, String text) {
        // Short queries remain supported; no scan fallback or minimum-length restriction.
        for (int size = 1; size <= 3; size++) grams(text, size).forEach(gram -> tokens.add(prefix + gram));
    }

    public static Criteria title(String term) { return contains("title", "t:", term); }
    public static Criteria author(String term) { return contains("authors", "a:", term); }
    public static Criteria global(String term) { return new Criteria().orOperator(title(term), author(term)); }

    private static Criteria contains(String field, String prefix, String term) {
        String text = normalize(term.trim());
        if (text.codePointCount(0, text.length()) <= 3) {
            // One stored gram is already an exact substring proof, including within one author.
            return Criteria.where("search.grams").is(prefix + text);
        }
        List<String> tokens = grams(text, Math.min(3, text.codePointCount(0, text.length()))).stream()
                // Prefer interior letter fragments over common word separators as the leading bound.
                .sorted(Comparator.comparing((String gram) -> gram.codePoints().anyMatch(Character::isWhitespace)))
                .map(gram -> prefix + gram).toList();
        return new Criteria().andOperator(Criteria.where("search.grams").all(tokens),
                Criteria.where("search." + field).regex(Pattern.quote(text)));
    }

    @Override
    public Object onBeforeSave(Object entity, Document document, String collection) {
        if (entity instanceof BookMongoEntity book && "books".equals(collection)) {
            document.put("search", fields(book.getTitle(), book.getAuthors()));
        }
        return entity;
    }

    @Override
    public void run(ApplicationArguments args) {
        mongo.indexOps("books").ensureIndex(new Index().on("search.grams", Sort.Direction.ASC)
                .on("_id", Sort.Direction.ASC).named(INDEX));
        mongo.indexOps("books").ensureIndex(new Index().on("search.version", Sort.Direction.ASC)
                .on("_id", Sort.Direction.ASC).named("book_search_version_idx"));
        backfill();
        ready = true;
    }

    /** Only reads identity fields. Each checkpoint is atomic with the derived fields and safe to resume. */
    public void backfill() {
        long started = System.nanoTime();
        long updated = 0;
        var collection = mongo.getCollection("books");
        while (true) {
            var batch = collection.find(Filters.ne("search.version", VERSION))
                    .projection(new Document("title", 1).append("authors", 1))
                    .sort(new Document("_id", 1)).limit(500).into(new ArrayList<>());
            if (batch.isEmpty()) break;
            List<WriteModel<Document>> writes = new ArrayList<>();
            for (Document book : batch) {
                // A concurrent save regenerates its own index. Never overwrite it with this older snapshot.
                var filter = Filters.and(Filters.eq("_id", book.get("_id")), Filters.ne("search.version", VERSION),
                        Filters.eq("title", book.get("title")), Filters.eq("authors", book.get("authors")));
                writes.add(new UpdateOneModel<>(filter, new Document("$set", new Document("search",
                        fields(book.getString("title"), book.getList("authors", String.class))))));
            }
            updated += collection.bulkWrite(writes, new com.mongodb.client.model.BulkWriteOptions().ordered(false)).getModifiedCount();
            log.info("Book substring index: {} existing books updated", updated);
        }
        log.info("Book substring index ready: updated={} elapsedMs={}", updated, (System.nanoTime() - started) / 1_000_000);
    }
}
