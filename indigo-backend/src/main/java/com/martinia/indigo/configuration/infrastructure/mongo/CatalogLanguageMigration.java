package com.martinia.indigo.configuration.infrastructure.mongo;

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
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
@Slf4j
public class CatalogLanguageMigration implements ApplicationRunner {
	private static final String VERSION = "catalog-languages-v1";
	private final MongoTemplate mongoTemplate;

	@Override
	public void run(ApplicationArguments args) {
		var migrations = mongoTemplate.getCollection("indigo_migrations");
		if (migrations.find(new Document("_id", VERSION)).first() != null) return;
		for (String name : List.of("authors", "tags")) {
			var collection = mongoTemplate.getCollection(name);
			List<WriteModel<Document>> updates = new ArrayList<>();
			try (var cursor = collection.find().projection(new Document("numBooks", 1)).batchSize(500).iterator()) {
				while (cursor.hasNext()) {
					Document document = cursor.next();
					updates.add(new UpdateOneModel<>(new Document("_id", document.get("_id")),
							new Document("$set", new Document("catalogLanguages", CatalogLanguages.from(document)))));
					if (updates.size() == 500) {
						collection.bulkWrite(updates);
						updates.clear();
					}
				}
			}
			if (!updates.isEmpty()) collection.bulkWrite(updates);
		}
		migrations.insertOne(new Document("_id", VERSION).append("completedAt", new java.util.Date()));
		log.info("Catalog language filters initialized for authors and categories");
	}
}
