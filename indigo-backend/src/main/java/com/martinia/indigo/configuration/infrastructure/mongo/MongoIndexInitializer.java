package com.martinia.indigo.configuration.infrastructure.mongo;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.util.LanguageCodeUtils;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MongoIndexInitializer implements ApplicationRunner {

	private static final Set<String> BASE_LANGUAGES = Set.of(
			"es", "spa", "en", "eng", "fr", "fra", "de", "deu", "it", "ita", "pt", "por", "ca", "cat"
	);

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

	@org.springframework.beans.factory.annotation.Value("${spring.data.mongodb.warm-up-on-startup:true}")
	private boolean warmUpOnStartup;

	@Override
	public void run(ApplicationArguments args) {
		try {
			ensureAuthorIndexes();
			ensureTagIndexes();
			ensureBookIndexes();
			ensureLanguageIndexes(BASE_LANGUAGES);

			if (warmUpOnStartup) {
				CompletableFuture.runAsync(this::warmUpCachesAndDynamicIndexes);
			}
		}
		catch (Exception e) {
			log.warn("Non-fatal error while ensuring MongoDB indexes: {}", e.getMessage());
		}
	}

	private void ensureAuthorIndexes() {
		try {
			IndexOperations ops = mongoTemplate.indexOps(AuthorMongoEntity.class);
			ops.ensureIndex(new Index().on("name", Sort.Direction.ASC).named("author_name_idx"));
			ops.ensureIndex(new CompoundIndexDefinition(new Document("numBooks.total", -1)).named("author_numbooks_total_idx"));
			log.info("Ensured author indexes in MongoDB");
		}
		catch (Exception e) {
			log.warn("Could not ensure author indexes: {}", e.getMessage());
		}
	}

	private void ensureTagIndexes() {
		try {
			IndexOperations ops = mongoTemplate.indexOps(TagMongoEntity.class);
			ops.ensureIndex(new Index().on("name", Sort.Direction.ASC).named("tag_name_idx"));
			ops.ensureIndex(new CompoundIndexDefinition(new Document("numBooks.total", -1)).named("tag_numbooks_total_idx"));
			log.info("Ensured tag indexes in MongoDB");
		}
		catch (Exception e) {
			log.warn("Could not ensure tag indexes: {}", e.getMessage());
		}
	}

	private void ensureBookIndexes() {
		try {
			IndexOperations ops = mongoTemplate.indexOps(BookMongoEntity.class);
			ops.ensureIndex(new CompoundIndexDefinition(new Document("languages", 1).append("serie.name", 1)).named("language_serie_idx"));
			ops.ensureIndex(new CompoundIndexDefinition(new Document("serie.name", 1).append("languages", 1)).named("serie_language_idx"));
			ops.ensureIndex(new CompoundIndexDefinition(new Document("serie.name", 1).append("serie.index", 1)).named("serie_name_index_idx"));
			ops.ensureIndex(new CompoundIndexDefinition(new Document("languages", 1).append("pubDate", -1)).named("languages_pubdate_idx"));
			ops.ensureIndex(new CompoundIndexDefinition(new Document("languages", 1).append("rating", -1)).named("languages_rating_idx"));
			log.info("Ensured book indexes in MongoDB");
		}
		catch (Exception e) {
			log.warn("Could not ensure book indexes: {}", e.getMessage());
		}
	}

	private void ensureLanguageIndexes(Set<String> languages) {
		if (languages == null || languages.isEmpty()) {
			return;
		}
		IndexOperations authorOps = mongoTemplate.indexOps(AuthorMongoEntity.class);
		IndexOperations tagOps = mongoTemplate.indexOps(TagMongoEntity.class);

		for (String lang : languages) {
			if (lang == null || lang.isBlank() || lang.contains("$")) {
				continue;
			}
			try {
				String field = "numBooks.languages." + lang;
				authorOps.ensureIndex(new CompoundIndexDefinition(new Document(field, 1).append("name", 1)).sparse().named("author_lang_" + lang + "_name_idx"));
				authorOps.ensureIndex(new Index().on(field, Sort.Direction.DESC).sparse().named("author_lang_" + lang + "_idx"));

				tagOps.ensureIndex(new CompoundIndexDefinition(new Document(field, 1).append("name", 1)).sparse().named("tag_lang_" + lang + "_name_idx"));
				tagOps.ensureIndex(new Index().on(field, Sort.Direction.DESC).sparse().named("tag_lang_" + lang + "_idx"));
			}
			catch (Exception e) {
				log.debug("Could not ensure language index for {}: {}", lang, e.getMessage());
			}
		}
	}

	private void warmUpCachesAndDynamicIndexes() {
		try {
			List<String> bookLanguages = bookRepository.getBookLanguages();
			Set<String> allVariants = new LinkedHashSet<>(BASE_LANGUAGES);
			if (bookLanguages != null) {
				for (String bl : bookLanguages) {
					allVariants.addAll(LanguageCodeUtils.variants(bl));
				}
			}
			ensureLanguageIndexes(allVariants);

			// Pre-warm counts and series queries
			authorRepository.count(List.of());
			tagRepository.count(List.of());
			bookRepository.getSeriesPage(List.of(), 0, 60, "_id", "asc");

			if (bookLanguages != null) {
				for (String bl : bookLanguages) {
					List<String> langList = List.of(bl);
					authorRepository.count(langList);
					tagRepository.count(langList);
					bookRepository.getSeriesPage(langList, 0, 60, "_id", "asc");
				}
			}
			log.info("Caches pre-warmed for {} book languages", bookLanguages != null ? bookLanguages.size() : 0);
		}
		catch (Exception e) {
			log.debug("Warm-up completed with notice: {}", e.getMessage());
		}
	}
}
