package com.martinia.indigo.configuration.infrastructure.mongo;

import com.martinia.indigo.common.util.LanguageCodeUtils;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** One-time, restartable migration. Updates only language fields and derived catalog counts. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class BookLanguageMigration implements ApplicationRunner {
	private static final String VERSION = "base-book-languages-v1";
	private final MongoTemplate mongoTemplate;

	@Override
	public void run(ApplicationArguments args) {
		var migrations = mongoTemplate.getCollection("indigo_migrations");
		if (migrations.find(new Document("_id", VERSION)).first() != null) return;
		normalize("books", "languages");
		normalize("users", "languageBooks");
		// Rebuild from books, not by summing old counters: es-ES + es-AR in one book counts once.
		Map<String, Document> authors = new HashMap<>();
		Map<String, Document> tags = new HashMap<>();
		try (var cursor = mongoTemplate.getCollection("books").find()
				.projection(new Document("authors", 1).append("tags", 1).append("languages", 1))
				.batchSize(500).iterator()) {
			while (cursor.hasNext()) {
				Document book = cursor.next();
				List<String> languages = LanguageCodeUtils.normalizeAll(book.getList("languages", String.class));
				accumulate(authors, book.getList("authors", String.class), languages, false);
				accumulate(tags, book.getList("tags", String.class), languages, true);
			}
		}
		updateCounts("authors", authors);
		updateCounts("tags", tags);
		migrations.replaceOne(new Document("_id", VERSION), new Document("_id", VERSION)
				.append("completedAt", new java.util.Date()), new com.mongodb.client.model.ReplaceOptions().upsert(true));
		log.info("Book language migration completed; author and category counts rebuilt");
	}

	private void normalize(String collection, String field) {
		var target = mongoTemplate.getCollection(collection);
		List<WriteModel<Document>> updates = new ArrayList<>();
		try (var cursor = target.find().projection(new Document(field, 1)).batchSize(500).iterator()) {
			while (cursor.hasNext()) {
				Document item = cursor.next();
				List<String> original = item.getList(field, String.class);
				if (original == null) continue;
				List<String> normalized = LanguageCodeUtils.normalizeAll(original);
				if (!original.equals(normalized)) {
					updates.add(new UpdateOneModel<>(new Document("_id", item.get("_id")),
							new Document("$set", new Document(field, normalized))));
				}
				if (updates.size() == 500) {
					target.bulkWrite(updates);
					updates.clear();
				}
			}
		}
		if (!updates.isEmpty()) target.bulkWrite(updates);
	}

	private String nameKey(String name, boolean category) {
		if (name == null) return "";
		if (category) return name.trim().toLowerCase(java.util.Locale.ROOT);
		return name.equalsIgnoreCase("VV., AA.") ? "AA. VV." : name;
	}

	private void accumulate(Map<String, Document> counts, List<String> names, List<String> languages, boolean category) {
		if (names == null) return;
		for (String name : names.stream().map(value -> nameKey(value, category)).distinct().toList()) {
			if (name == null || name.isBlank()) continue;
			Document stats = counts.computeIfAbsent(name,
					ignored -> new Document("total", 0).append("languages", new Document()));
			stats.put("total", stats.getInteger("total") + 1);
			Document byLanguage = stats.get("languages", Document.class);
			for (String language : languages) byLanguage.put(language, byLanguage.getInteger(language, 0) + 1);
		}
	}

	private void updateCounts(String collection, Map<String, Document> counts) {
		var target = mongoTemplate.getCollection(collection);
		List<WriteModel<Document>> updates = new ArrayList<>();
		try (var cursor = target.find().projection(new Document("name", 1)).batchSize(500).iterator()) {
			while (cursor.hasNext()) {
				Document item = cursor.next();
				Document stats = counts.getOrDefault(nameKey(item.getString("name"), "tags".equals(collection)),
						new Document("total", 0).append("languages", new Document()));
				updates.add(new UpdateOneModel<>(new Document("_id", item.get("_id")),
						new Document("$set", new Document("numBooks", stats))));
				if (updates.size() == 500) {
					target.bulkWrite(updates);
					updates.clear();
				}
			}
		}
		if (!updates.isEmpty()) target.bulkWrite(updates);
	}
}
