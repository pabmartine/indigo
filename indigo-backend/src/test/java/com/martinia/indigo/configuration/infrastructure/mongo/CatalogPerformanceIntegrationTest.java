package com.martinia.indigo.configuration.infrastructure.mongo;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.repositories.CustomAuthorRepositoryImpl;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.repositories.CustomBookRepositoryImpl;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.tag.domain.ports.repositories.CustomTagRepositoryImpl;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import com.martinia.indigo.tag.infrastructure.mongo.mappers.TagMongoMapper;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Testcontainers
class CatalogPerformanceIntegrationTest {

	@Container
	static final MongoDBContainer MONGO = new MongoDBContainer("mongo:4.0.10");

	@Test
	void recommendationsShareCountsAndRankingWithoutDuplicatesOrHeavyFields() {
		try (var client = MongoClients.create(MONGO.getReplicaSetUrl())) {
			MongoTemplate template = new MongoTemplate(client, "recommendations_performance");
			var users = mock(com.martinia.indigo.user.domain.ports.repositories.UserRepository.class);
			var user = UserMongoEntity.builder().username("reader").languageBooks(List.of("en-GB")).build();
			when(users.findByUsername("reader")).thenReturn(java.util.Optional.of(user));
			var query = new com.martinia.indigo.book.domain.ports.repositories.UserRecommendationQuery(template, users);
			var a = new org.bson.types.ObjectId();
			var b = new org.bson.types.ObjectId();
			var foreign = new org.bson.types.ObjectId();
			var source = new org.bson.types.ObjectId();
			template.getCollection("books").insertMany(List.of(
					new Document("_id", a).append("title", "Z").append("path", "a").append("languages", List.of("eng"))
							.append("image", "large image").append("comment", "long comment"),
					new Document("_id", b).append("title", "A").append("path", "b").append("languages", List.of("en")),
					new Document("_id", foreign).append("path", "fr").append("languages", List.of("fr")),
					new Document("_id", source).append("path", "source").append("languages", List.of("en"))
							.append("recommendations", List.of(a.toHexString(), b.toHexString(), b.toHexString(),
									foreign.toHexString(), source.toHexString(), "invalid", new org.bson.types.ObjectId().toHexString())),
					new Document("path", "source2").append("recommendations", List.of(a.toHexString()))));
			for (String path : List.of("source", "source", "source", "source2")) {
				template.getCollection("notifications").insertOne(new Document("user", "reader").append("type", "KINDLE")
						.append("status", "SEND").append("kindle", new Document("book", path)));
			}
			var first = query.page("reader", 0, 1, "count", "desc", true);
			assertThat(first.total()).isEqualTo(2);
			assertThat(first.items()).extracting(BookMongoEntity::getId).containsExactly(a.toHexString());
			assertThat(first.items().get(0).getImage()).isNull();
			assertThat(first.items().get(0).getComment()).isNull();
			assertThat(query.page("reader", 1, 1, "count", "desc", true).items())
					.extracting(BookMongoEntity::getId).containsExactly(b.toHexString());
			assertThat(query.page("reader", 0, 20, "title", "asc", true).items())
					.extracting(BookMongoEntity::getId).containsExactly(b.toHexString(), a.toHexString());
			assertThat(query.count("reader")).isEqualTo(first.total());
			assertThat(query.page("missing", 0, 20, "count", "desc", true).total()).isZero();
			user.setLanguageBooks(List.of());
			assertThat(query.count("reader")).isEqualTo(3);
		}
	}

	@Test
	void regionalLanguagesAreMigratedOnceAndNewWritesStayNormalized() {
		try (var client = MongoClients.create(MONGO.getReplicaSetUrl())) {
			MongoTemplate template = new MongoTemplate(client, "language_migration");
			template.getCollection("books").insertOne(new Document("languages", List.of("es-ES", "es_AR", "EN-en"))
					.append("authors", List.of("Author")).append("tags", List.of("Tag")).append("image", "unchanged"));
			template.getCollection("users").insertOne(new Document("languageBooks", List.of("es_MX", "en-US")));
			template.getCollection("authors").insertOne(new Document("name", "Author"));
			template.getCollection("tags").insertOne(new Document("name", "Tag"));
			var migration = new BookLanguageMigration(template);
			migration.run(null);
			migration.run(null);
			Document book = template.getCollection("books").find().first();
			assertThat(book.getList("languages", String.class)).containsExactly("es", "en");
			assertThat(book.getString("image")).isEqualTo("unchanged");
			assertThat(template.getCollection("users").find().first().getList("languageBooks", String.class))
					.containsExactly("es", "en");
			for (String collection : List.of("authors", "tags")) {
				Document counts = template.getCollection(collection).find().first().get("numBooks", Document.class);
				assertThat(counts.getInteger("total")).isEqualTo(1);
				assertThat(counts.get("languages", Document.class)).isEqualTo(new Document("es", 1).append("en", 1));
			}
			template.setEntityCallbacks(org.springframework.data.mapping.callback.EntityCallbacks.create(new NormalizeBookLanguagesCallback()));
			var saved = template.save(BookMongoEntity.builder().languages(List.of("pt_BR", "pt-PT", "zh-Hant-TW")).build());
			assertThat(saved.getLanguages()).containsExactly("pt", "zh");
			assertThat(template.findById(saved.getId(), BookMongoEntity.class).getLanguages()).containsExactly("pt", "zh");
		}
	}

	@Test
	void recommendationGenerationRanksOverlapAndAcceptsMissingPageAndDateMetadata() {
		try (var client = MongoClients.create(MONGO.getReplicaSetUrl())) {
			MongoTemplate template = new MongoTemplate(client, "recommendation_generation");
			var source = template.save(BookMongoEntity.builder().tags(List.of("A", "B")).authors(List.of("Author"))
					.languages(List.of("en")).build());
			var best = template.save(BookMongoEntity.builder().tags(List.of("A", "B")).authors(List.of("Author"))
					.languages(List.of("eng")).build());
			var related = template.save(BookMongoEntity.builder().tags(List.of("A")).languages(List.of("en")).build());
			template.save(BookMongoEntity.builder().tags(List.of("A", "B")).languages(List.of("fr")).build());
			var repository = new CustomBookRepositoryImpl();
			ReflectionTestUtils.setField(repository, "mongoTemplate", template);
			assertThat(repository.getRecommendationsByBook(source)).extracting(BookMongoEntity::getId)
					.containsExactly(best.getId(), related.getId());
			assertThat(repository.getRecommendationsByBook(BookMongoEntity.builder().build())).isEmpty();
		}
	}

	@Test
	void coldCatalogQueriesUseIndexesAndDoNotLoadImages() {
		try (var client = MongoClients.create(MONGO.getReplicaSetUrl())) {
			MongoTemplate template = spy(new MongoTemplate(client, "catalog_performance"));
			for (Class<?> type : List.of(AuthorMongoEntity.class, TagMongoEntity.class,
					BookMongoEntity.class, UserMongoEntity.class)) {
				template.getConverter().getMappingContext().getPersistentEntity(type);
			}
			// Simulate the names used by older installations.
			template.indexOps(AuthorMongoEntity.class).ensureIndex(new Index().on("name", Sort.Direction.ASC).named("name"));
			template.indexOps(TagMongoEntity.class).ensureIndex(new Index().on("name", Sort.Direction.ASC).named("tag_name_idx"));
			BookRepository books = mock(BookRepository.class);
			when(books.getBookLanguages()).thenReturn(List.of("en", "eng"));
			MongoIndexInitializer initializer = new MongoIndexInitializer(template,
					mock(AuthorRepository.class), mock(TagRepository.class), books);
			initializer.run(null);
			initializer.run(null); // Idempotent even with legacy names and text indexes.
			assertThat(template.indexOps(UserMongoEntity.class).getIndexInfo())
					.anyMatch(index -> index.isUnique());
			assertThat(template.indexOps(BookMongoEntity.class).getIndexInfo())
					.anyMatch(index -> index.getIndexFields().stream().anyMatch(field -> field.isText()));

			for (String collection : List.of("authors", "tags")) {
				List<Document> documents = new ArrayList<>();
				for (int i = 0; i < 120; i++) {
					documents.add(new Document("_id", "item-" + i).append("name", String.format("Name %03d", i))
						.append("image", "x".repeat(16_384)).append("description", "long biography")
						.append("numBooks", new Document("total", 1)
								.append("languages", new Document(i % 2 == 0 ? "en" : "eng", 1))));
				}
				template.getCollection(collection).insertMany(documents);
				Document explain = template.executeCommand(new Document("explain",
						new Document("find", collection).append("filter", new Document())
								.append("sort", new Document("name", 1)).append("limit", 20))
						.append("verbosity", "executionStats"));
				assertThat(explain.get("queryPlanner", Document.class).get("winningPlan").toString())
						.contains("IXSCAN").doesNotContain("COLLSCAN").doesNotContain("stage=SORT");
				assertThat(explain.get("executionStats", Document.class).getInteger("totalDocsExamined")).isEqualTo(20);
				Document filtered = template.executeCommand(new Document("explain",
						new Document("find", collection).append("filter", new Document("$or", List.of(
								new Document("numBooks.languages.en", new Document("$gt", 0)),
								new Document("numBooks.languages.eng", new Document("$gt", 0))))))
						.append("verbosity", "executionStats"));
				assertThat(filtered.get("queryPlanner", Document.class).get("winningPlan").toString())
						.contains("IXSCAN").doesNotContain("COLLSCAN");
			}
			CustomAuthorRepositoryImpl authors = new CustomAuthorRepositoryImpl();
			ReflectionTestUtils.setField(authors, "mongoTemplate", template);
			ReflectionTestUtils.setField(authors, "authorMongoMapper", Mappers.getMapper(AuthorMongoMapper.class));
			CustomTagRepositoryImpl tags = new CustomTagRepositoryImpl();
			ReflectionTestUtils.setField(tags, "mongoTemplate", template);
			ReflectionTestUtils.setField(tags, "tagMongoMapper", Mappers.getMapper(TagMongoMapper.class));
			var page = PageRequest.of(0, 20, Sort.by("name"));
			var authorPage = authors.findSummaryPage(List.of("en"), page);
			var tagPage = tags.findSummaryPage(List.of("en"), page);
			assertThat(authorPage.total()).isEqualTo(120);
			assertThat(tagPage.total()).isEqualTo(120);
			assertThat(authorPage.items()).hasSize(20).allMatch(author -> author.getImage() == null && author.getDescription() == null);
			assertThat(tagPage.items()).hasSize(20).allMatch(tag -> tag.getImage() == null);

			clearInvocations(template);
			assertThat(authors.findSummaryPage(List.of(), PageRequest.of(1, 100)).total()).isEqualTo(120);
			assertThat(tags.findSummaryPage(List.of(), PageRequest.of(1, 100)).total()).isEqualTo(120);
			verify(template, never()).count(any(org.springframework.data.mongodb.core.query.Query.class), any(Class.class));
			// An empty page beyond the end cannot infer the total from its offset.
			assertThat(authors.findSummaryPage(List.of(), PageRequest.of(2, 100)).total()).isEqualTo(120);
			assertThat(tags.findSummaryPage(List.of(), PageRequest.of(2, 100)).total()).isEqualTo(120);

			template.getCollection("books").insertMany(List.of(
					new Document("languages", List.of("en", "es")), new Document("languages", List.of("en"))));
			CustomBookRepositoryImpl bookQueries = new CustomBookRepositoryImpl();
			ReflectionTestUtils.setField(bookQueries, "mongoTemplate", template);
			assertThat(bookQueries.getBookLanguages()).containsExactlyInAnyOrder("en", "es");
		}
	}
}
