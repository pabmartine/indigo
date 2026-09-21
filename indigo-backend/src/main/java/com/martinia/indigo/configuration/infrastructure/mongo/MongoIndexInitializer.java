package com.martinia.indigo.configuration.infrastructure.mongo;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class MongoIndexInitializer implements ApplicationRunner {

	private final MongoTemplate mongoTemplate;
	private final AuthorRepository authorRepository;
	private final TagRepository tagRepository;
	private final BookRepository bookRepository;

	public MongoIndexInitializer(
			MongoTemplate mongoTemplate,
			@Lazy AuthorRepository authorRepository,
			@Lazy TagRepository tagRepository,
			@Lazy BookRepository bookRepository) {
		this.mongoTemplate = mongoTemplate;
		this.authorRepository = authorRepository;
		this.tagRepository = tagRepository;
		this.bookRepository = bookRepository;
	}

	@org.springframework.beans.factory.annotation.Value("${spring.data.mongodb.warm-up-on-startup:false}")
	private boolean warmUpOnStartup;

	@Override
	public void run(ApplicationArguments args) {
		// With automatic creation disabled, retain all declared indexes, including text and unique indexes.
		var context = mongoTemplate.getConverter().getMappingContext();
		var resolver = new MongoPersistentEntityIndexResolver(context);
		for (var entity : context.getPersistentEntities()) {
			if (entity.isAnnotationPresent(org.springframework.data.mongodb.core.mapping.Document.class)) {
				for (IndexDefinition index : resolver.resolveIndexFor(entity.getType())) {
					ensureIndex(entity.getType(), index);
				}
			}
		}
		for (Class<?> type : List.of(AuthorMongoEntity.class, TagMongoEntity.class)) {
			ensureIndex(type, new Index().on("numBooks.total", Sort.Direction.DESC));
			for (String sort : List.of("name", "numBooks.total")) {
				ensureIndex(type, new Index().on("catalogLanguages", Sort.Direction.ASC)
						.on(sort, Sort.Direction.ASC).on("_id", Sort.Direction.ASC));
			}
		}
		ensureIndex(BookMongoEntity.class,
				new Index().on("serie.name", Sort.Direction.ASC).on("serie.index", Sort.Direction.ASC));
		ensureIndex(BookMongoEntity.class, new Index().on("authors", Sort.Direction.ASC));
		ensureIndex(BookMongoEntity.class, new Index().on("languages", Sort.Direction.ASC)
				.on("serie.name", Sort.Direction.ASC));
		try {
			List<String> languages = bookRepository.getBookLanguages();
			if (warmUpOnStartup) {
				// Explicit opt-in on the startup thread, never the JVM common pool.
				authorRepository.count(List.of());
				tagRepository.count(List.of());
				for (String language : languages) {
					authorRepository.count(List.of(language));
					tagRepository.count(List.of(language));
				}
			}
		}
		catch (Exception e) {
			log.warn("Could not initialize language indexes/counts", e);
		}
	}

	void ensureIndex(Class<?> type, IndexDefinition index) {
		try {
			List<Document> existing = mongoTemplate.getCollection(mongoTemplate.getCollectionName(type))
					.listIndexes().into(new ArrayList<>());
			if (existing.stream().noneMatch(current -> equivalent(current, index))) {
				mongoTemplate.indexOps(type).ensureIndex(index);
			}
		}
		catch (Exception e) {
			// One conflict must not prevent creation of the remaining indexes.
			log.warn("Could not ensure index {} on {}", index.getIndexKeys(), type.getSimpleName(), e);
		}
	}

	static boolean equivalent(Document existing, IndexDefinition requested) {
		Document keys = requested.getIndexKeys();
		Document options = requested.getIndexOptions();
		boolean sameKeys = keys.entrySet().stream().toList()
				.equals(existing.get("key", Document.class).entrySet().stream().toList());
		// MongoDB represents text keys internally as _fts/_ftsx.
		if (keys.containsValue("text")) {
			Document weights = existing.get("weights", Document.class);
			Document requestedWeights = options.get("weights", new Document());
			sameKeys = weights != null && weights.keySet().equals(keys.keySet())
					&& weights.entrySet().stream().allMatch(entry ->
							((Number) entry.getValue()).doubleValue()
									== ((Number) requestedWeights.getOrDefault(entry.getKey(), 1)).doubleValue())
					&& Objects.equals(existing.getOrDefault("default_language", "english"),
							options.getOrDefault("default_language", "english"))
					&& Objects.equals(existing.getOrDefault("language_override", "language"),
							options.getOrDefault("language_override", "language"));
		}
		return sameKeys
				&& Objects.equals(existing.getBoolean("unique", false), options.getBoolean("unique", false))
				&& Objects.equals(existing.getBoolean("sparse", false), options.getBoolean("sparse", false))
				&& Objects.equals(existing.get("partialFilterExpression"), options.get("partialFilterExpression"))
				&& Objects.equals(existing.get("expireAfterSeconds"), options.get("expireAfterSeconds"))
				&& Objects.equals(existing.get("collation"), options.get("collation"));
	}
}
