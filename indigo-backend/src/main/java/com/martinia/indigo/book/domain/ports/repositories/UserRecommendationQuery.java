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
		pipeline.add(new Document("$facet", new Document("items", List.of(
				new Document("$sort", sorting), new Document("$skip", (long) page * size), new Document("$limit", size)))
				.append("total", List.of(new Document("$count", "value")))));
		Document result = mongoTemplate.getCollection("books").aggregate(pipeline).allowDiskUse(true).first();
		if (result == null) {
			return new Result(List.of(), 0);
		}
		List<BookMongoEntity> items = result.getList("items", Document.class).stream()
				.map(document -> mongoTemplate.getConverter().read(BookMongoEntity.class, document)).toList();
		List<Document> totals = result.getList("total", Document.class);
		long total = totals.isEmpty() ? 0 : ((Number) totals.get(0).get("value")).longValue();
		long elapsedMs = (System.nanoTime() - started) / 1_000_000;
		var timingLog = elapsedMs >= 1000 ? log.atInfo() : log.atDebug();
		timingLog.log("Recommendation page={} size={} items={} total={} summary={} totalMs={}",
				page, size, items.size(), total, summary, elapsedMs);
		return new Result(items, total);
	}

	public long count(String username) {
		List<Document> pipeline = selection(username, true);
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
		Document candidateFilter = new Document("$expr", new Document("$eq", List.of("$_id", "$$candidate")))
				.append("path", new Document("$nin", paths));
		List<String> languages = user.get().getLanguageBooks();
		if (languages != null && !languages.isEmpty()) {
			candidateFilter.append("languages", new Document("$in", languages.stream()
					.flatMap(language -> LanguageCodeUtils.variants(language).stream()).distinct().toList()));
		}
		List<Document> lookup = new ArrayList<>();
		lookup.add(new Document("$match", candidateFilter));
		if (summary) {
			lookup.add(new Document("$project", new Document("_id", 1).append("title", 1).append("path", 1)
					.append("authors", 1).append("serie", 1).append("pubDate", 1).append("pages", 1)
					.append("rating", 1).append("tags", 1).append("languages", 1).append("lastModified", 1)));
		}
		return new ArrayList<>(List.of(
				new Document("$match", new Document("path", new Document("$in", paths))),
				new Document("$project", new Document("recommendations", 1)),
				new Document("$unwind", "$recommendations"),
				// Count distinct source books, not sends or duplicate references within one book.
				new Document("$group", new Document("_id", new Document("source", "$_id").append("target",
						new Document("$convert", new Document("input", "$recommendations").append("to", "objectId")
								.append("onError", null).append("onNull", null))))),
				new Document("$group", new Document("_id", "$_id.target").append("count", new Document("$sum", 1))),
				new Document("$match", new Document("_id", new Document("$ne", null))),
				new Document("$lookup", new Document("from", "books").append("let", new Document("candidate", "$_id"))
						.append("pipeline", lookup).append("as", "book")),
				new Document("$unwind", "$book"),
				new Document("$replaceRoot", new Document("newRoot", new Document("$mergeObjects",
						List.of("$book", new Document("count", "$count")))))
		));
	}
}
