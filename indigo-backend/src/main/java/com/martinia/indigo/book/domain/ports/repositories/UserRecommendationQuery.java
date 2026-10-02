package com.martinia.indigo.book.domain.ports.repositories;

import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.util.LanguageCodeUtils;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Shared selection rules for the legacy endpoints and the lightweight "For you" page. */
@Repository
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class UserRecommendationQuery {
	private static final Set<String> SORTS = Set.of("count", "title", "pubDate", "rating", "pages", "path", "lastModified", "_id");
	private final MongoTemplate mongoTemplate;
	private final UserRepository userRepository;

	public record Result(List<BookMongoEntity> items, long total) {}

	public Result page(String username, int page, int size, String sort, String order, boolean summary) {
		long started = System.nanoTime();
		if (page < 0 || size < 1 || size > 200) {
			throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 200");
		}
		List<Document> pipeline = selection(username, summary);
		long preparationMs = (System.nanoTime() - started) / 1_000_000;
		if (pipeline.isEmpty()) {
			return new Result(List.of(), 0);
		}
		String field = "id".equals(sort) ? "_id" : sort;
		if (field == null || !SORTS.contains(field)) {
			field = "count";
		}
		Document sorting = new Document(field, "asc".equalsIgnoreCase(order) ? 1 : -1);
		if (!"_id".equals(field)) {
			sorting.append("_id", 1);
		}
		List<Document> itemsPipeline = new ArrayList<>(List.of(
				new Document("$sort", sorting), new Document("$skip", (long) page * size), new Document("$limit", size)));
		if (!summary) {
			itemsPipeline.add(new Document("$lookup", new Document("from", "books").append("localField", "_id")
					.append("foreignField", "_id").append("as", "book")));
			itemsPipeline.add(new Document("$unwind", "$book"));
			itemsPipeline.add(bookWithCount());
		}
		pipeline.add(new Document("$facet", new Document("items", itemsPipeline)
				.append("total", List.of(new Document("$count", "value")))));
		long aggregateStarted = System.nanoTime();
		Document result = mongoTemplate.getCollection("books").aggregate(pipeline).allowDiskUse(true).first();
		long aggregateMs = (System.nanoTime() - aggregateStarted) / 1_000_000;
		if (result == null) {
			return new Result(List.of(), 0);
		}
		List<BookMongoEntity> items = result.getList("items", Document.class).stream()
				.map(document -> mongoTemplate.getConverter().read(BookMongoEntity.class, document)).toList();
		List<Document> totals = result.getList("total", Document.class);
		long total = totals.isEmpty() ? 0 : ((Number) totals.get(0).get("value")).longValue();
		long elapsedMs = (System.nanoTime() - started) / 1_000_000;
		var timingLog = elapsedMs >= 1000 ? log.atInfo() : log.atDebug();
		timingLog.log("Recommendation page={} size={} items={} total={} summary={} preparationMs={} aggregateMs={} totalMs={}",
				page, size, items.size(), total, summary, preparationMs, aggregateMs, elapsedMs);
		return new Result(items, total);
	}

	public long count(String username) {
		List<Document> pipeline = selection(username, false);
		if (pipeline.isEmpty()) {
			return 0;
		}
		pipeline.add(new Document("$count", "value"));
		Document result = mongoTemplate.getCollection("books").aggregate(pipeline).allowDiskUse(true).first();
		return result == null ? 0 : ((Number) result.get("value")).longValue();
	}

	private List<Document> selection(String username, boolean summary) {
		if (username == null || username.isBlank()) {
			return new ArrayList<>();
		}
		var user = userRepository.findByUsername(username);
		if (user.isEmpty()) {
			return new ArrayList<>();
		}
		// Failed sends do not express a completed reading choice. Keep legacy records without a status.
		Query history = Query.query(Criteria.where("user").is(username).and("type").is("KINDLE")
				.and("status").ne("NOT_SEND").and("kindle.book").ne(null));
		List<String> paths = mongoTemplate.findDistinct(history, "kindle.book", NotificationMongoEntity.class, String.class);
		if (paths.isEmpty()) {
			return new ArrayList<>();
		}
		Document candidateFilter = new Document("book.path", new Document("$nin", paths));
		List<String> languages = user.get().getLanguageBooks();
		if (languages != null && !languages.isEmpty()) {
			candidateFilter.append("book.languages", new Document("$in", languages.stream()
					.flatMap(language -> LanguageCodeUtils.variants(language).stream()).distinct().toList()));
		}
		Document targets = new Document("$map", new Document("input",
				new Document("$ifNull", List.of("$recommendations", List.of())))
				.append("as", "target").append("in", new Document("$convert",
						new Document("input", "$$target").append("to", "objectId").append("onError", null).append("onNull", null))));
		Document projection = new Document("count", 1).append("book._id", 1).append("book.title", 1)
				.append("book.path", 1).append("book.pubDate", 1).append("book.pages", 1)
				.append("book.rating", 1).append("book.lastModified", 1);
		if (summary) {
			projection.append("book.authors", 1).append("book.serie", 1).append("book.tags", 1).append("book.languages", 1);
		}
		return new ArrayList<>(List.of(
				new Document("$match", new Document("path", new Document("$in", paths))),
				new Document("$project", new Document("recommendations", new Document("$setUnion", List.of(targets, List.of())))),
				new Document("$unwind", "$recommendations"),
				new Document("$match", new Document("recommendations", new Document("$ne", null))),
				new Document("$group", new Document("_id", "$recommendations").append("count", new Document("$sum", 1))),
				new Document("$lookup", new Document("from", "books").append("localField", "_id")
						.append("foreignField", "_id").append("as", "book")),
				new Document("$unwind", "$book"),
				new Document("$match", candidateFilter),
				new Document("$project", projection),
				bookWithCount()
		));
	}

	private Document bookWithCount() {
		return new Document("$replaceRoot", new Document("newRoot", new Document("$mergeObjects",
				List.of("$book", new Document("count", "$count")))));
	}
}
