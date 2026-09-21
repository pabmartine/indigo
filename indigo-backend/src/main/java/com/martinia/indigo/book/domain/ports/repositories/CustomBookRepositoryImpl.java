package com.martinia.indigo.book.domain.ports.repositories;

import com.martinia.indigo.book.domain.model.BookPageData;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.common.domain.model.Search;
import com.martinia.indigo.common.util.LanguageCodeUtils;
import com.martinia.indigo.serie.domain.model.SeriePageData;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
@Slf4j
public class CustomBookRepositoryImpl implements CustomBookRepository {

	private static final long SERIES_CACHE_TTL_MS = 10 * 60 * 1000L;
	private final ConcurrentMap<String, CachedSeriesPage> seriesPageCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, CachedNumSeries> numSeriesCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, CachedNumBooksBySerie> numBooksBySerieCache = new ConcurrentHashMap<>();

	private record CachedSeriesPage(SeriePageData data, long timestamp) {
		boolean isExpired() {
			return System.currentTimeMillis() - timestamp > SERIES_CACHE_TTL_MS;
		}
	}

	private record CachedNumSeries(long count, long timestamp) {
		boolean isExpired() {
			return System.currentTimeMillis() - timestamp > SERIES_CACHE_TTL_MS;
		}
	}

	private record CachedNumBooksBySerie(Map<String, Long> map, long timestamp) {
		boolean isExpired() {
			return System.currentTimeMillis() - timestamp > SERIES_CACHE_TTL_MS;
		}
	}

	private static final Set<String> ALLOWED_SORT_FIELDS = new LinkedHashSet<>(Arrays.asList(
			"count", "title", "path", "pubDate", "pages", "rating", "lastModified", "_id", "id"
	));

	@Resource
	private MongoTemplate mongoTemplate;

	@Resource
	private BookMongoMapper bookMongoMapper;

	private String collectionName = BookMongoEntity.class.getAnnotation(org.springframework.data.mongodb.core.mapping.Document.class)
			.collection();


	public long countBooks(Search search) {
		Query query = buildSearchQuery(search);
		return mongoTemplate.count(query, BookMongoEntity.class);
	}

	@Override
	public long countReviews() {
		List<Document> pipeline = Arrays.asList(
				new Document("$project", new Document("reviewsCount",
						new Document("$size", new Document("$ifNull", Arrays.asList("$reviews", Collections.emptyList()))))),
				new Document("$group", new Document("_id", null).append("total", new Document("$sum", "$reviewsCount")))
		);

		Document result = mongoTemplate.getCollection(collectionName).aggregate(pipeline).first();
		return result == null ? 0L : Long.parseLong(String.valueOf(result.get("total")));
	}

	public List<BookMongoEntity> findAll(Search search, int page, int size, String sort, String order) {

		Query query = buildSearchQuery(search)
				.with(PageRequest.of(page, size, Sort.by(resolveDirection(order), resolveSortField(sort))));
		excludeHeavyBookFields(query);

		return mongoTemplate.find(query, BookMongoEntity.class);

	}

	@Override
	public List<BookMongoEntity> findSummary(Search search, int page, int size, String sort, String order) {
		Query query = buildSearchQuery(search)
				.with(PageRequest.of(page, size, Sort.by(resolveDirection(order), resolveSortField(sort))));
		query.fields().include("_id").include("title").include("path").include("serie")
				.include("pubDate").include("pages").include("rating").include("authors")
				.include("tags").include("languages");
		return mongoTemplate.find(query, BookMongoEntity.class);
	}

	@Override
	public BookPageData findSummaryPage(Search search, int page, int size, String sort, String order) {
		long started = System.nanoTime();
		List<BookMongoEntity> entities = findSummary(search, page, size, sort, order);
		long queried = System.nanoTime();
		long total = countBooks(search);
		long counted = System.nanoTime();
		List<Book> items = entities.stream().map(this::mapEntityToDomain).toList();
		long finished = System.nanoTime();
		// No search text or book data in diagnostic logs.
		var timingLog = finished - started >= 1_000_000_000L ? log.atInfo() : log.atDebug();
		timingLog.log("Book summary page={} size={} items={} total={} queryMs={} countMs={} mappingMs={} totalMs={}",
				page, size, items.size(), total, (queried - started) / 1_000_000L,
				(counted - queried) / 1_000_000L, (finished - counted) / 1_000_000L,
				(finished - started) / 1_000_000L);
		return new BookPageData(items, total);
	}

	@Override
	public BookPageData findAllPage(Search search, int page, int size, String sort, String order) {
		// Keep sort/limit in the find query so MongoDB can use an ordered index.
		List<Book> items = findAll(search, page, size, sort, order).stream()
				.map(this::mapEntityToDomain).toList();
		return new BookPageData(items, countBooks(search));
	}

	@Override
	public List<BookMongoEntity> findCategoryBatch(String afterId) {
		return maintenanceBatch(afterId, "tags", "languages");
	}

	@Override
	public List<BookMongoEntity> findReconciliationBatch(String afterId) {
		return maintenanceBatch(afterId, "path", "tags", "identifiers", "isbn10", "isbn13");
	}

	private List<BookMongoEntity> maintenanceBatch(String afterId, String... fields) {
		Query query = new Query().with(Sort.by(Sort.Direction.ASC, "_id")).limit(100);
		if (afterId != null) query.addCriteria(Criteria.where("id").gt(afterId));
		query.fields().include("_id");
		for (String field : fields) query.fields().include(field);
		return mongoTemplate.find(query, BookMongoEntity.class);
	}

	@Override
	public List<BookMongoEntity> findSimilarCandidates(Search search, int page) {
		Query query = buildSearchQuery(search).with(PageRequest.of(page, 100, Sort.by("_id")));
		query.fields().include("_id").include("authors");
		return mongoTemplate.find(query, BookMongoEntity.class);
	}

	@Override
	public void updateReconciledMetadata(BookMongoEntity book, boolean tagsChanged, boolean identifiersChanged) {
		Update update = new Update();
		if (tagsChanged) update.set("tags", book.getTags());
		if (identifiersChanged) {
			update.set("identifiers", book.getIdentifiers()).set("isbn10", book.getIsbn10())
					.set("isbn13", book.getIsbn13()).set("metadataMatchStatus", null)
					.set("metadataMatchConfidence", null);
		}
		if (tagsChanged || identifiersChanged) updateBook(book.getId(), update);
	}

	@Override
	public void updateSimilar(String bookId, List<String> similar) {
		updateBook(bookId, new Update().set("similar", similar));
	}

	@Override
	public void updateRecommendations(String bookId, List<String> recommendations) {
		updateBook(bookId, new Update().set("recommendations", recommendations));
	}

	private void updateBook(String bookId, Update update) {
		mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(bookId)), update, BookMongoEntity.class);
	}

	@Override
	public List<BookMongoEntity> getRecommendationsByBook(BookMongoEntity book) {
		List<String> tags = book.getTags() == null ? List.of() : book.getTags();
		List<String> authors = book.getAuthors() == null ? List.of() : book.getAuthors();
		List<Criteria> affinities = new ArrayList<>();
		if (!tags.isEmpty()) affinities.add(Criteria.where("tags").in(tags));
		if (!authors.isEmpty()) affinities.add(Criteria.where("authors").in(authors));
		if (affinities.isEmpty()) return List.of();

		Criteria criteria = Criteria.where("id").ne(book.getId())
				.orOperator(affinities.toArray(Criteria[]::new));
		if (!CollectionUtils.isEmpty(book.getLanguages())) {
			criteria.and("languages").in(book.getLanguages().stream()
					.flatMap(language -> LanguageCodeUtils.variants(language).stream())
					.distinct().toList());
		}
		// Missing pages/dates must not exclude otherwise related books. Rank shared tags and authors.
		Document tagScore = new Document("$size", new Document("$setIntersection",
				List.of(new Document("$ifNull", List.of("$tags", List.of())), new Document("$literal", tags))));
		Document authorScore = new Document("$size", new Document("$setIntersection",
				List.of(new Document("$ifNull", List.of("$authors", List.of())), new Document("$literal", authors))));
		var aggregation = Aggregation.newAggregation(
				Aggregation.match(criteria),
				context -> new Document("$project", new Document("_id", 1).append("rating", 1).append("score",
						new Document("$add", List.of(new Document("$multiply", List.of(tagScore, 3)),
								new Document("$multiply", List.of(authorScore, 2)))))),
				Aggregation.sort(
						Sort.by(Direction.DESC, "score", "rating").and(Sort.by("_id"))),
				Aggregation.limit(200),
				Aggregation.project("_id"));
		return mongoTemplate.aggregate(aggregation, BookMongoEntity.class, BookMongoEntity.class).getMappedResults();
	}

	private Document buildSerieMatchDocument(List<String> languages) {
		Document match = new Document("serie.name", new Document("$gt", ""));
		if (languages != null && !languages.isEmpty()) {
			match.append("languages", new Document("$in", LanguageCodeUtils.expand(languages)));
		}
		return match;
	}

	@Override
	public Map<String, Long> getNumBooksBySerie(List<String> languages, int page, int size, String sort, String order) {
		String key = (languages == null ? "" : String.join(",", languages)) + ":" + page + ":" + size + ":" + sort + ":" + order;
		CachedNumBooksBySerie cached = numBooksBySerieCache.get(key);
		if (cached != null && !cached.isExpired()) {
			return cached.map();
		}

		Map<String, Long> map = new LinkedHashMap<>();

		List<Document> list = Arrays.asList(
				new Document("$match", buildSerieMatchDocument(languages)),
				new Document("$project", new Document("serie.name", 1L)),
				new Document("$group", new Document("_id", "$serie.name").append("count", new Document("$sum", 1L))),
				new Document("$sort", new Document("numBooks".equals(sort) ? "count" : "_id", "asc".equalsIgnoreCase(order) ? 1 : -1)),
				new Document("$skip", page * size), new Document("$limit", size));

		AggregateIterable<Document> data = mongoTemplate.getCollection(collectionName).aggregate(list);

		Iterator<Document> it = data.iterator();
		while (it.hasNext()) {
			Document document = it.next();
			String serie = document.get("_id").toString();
			Long numBooks = Long.parseLong(document.get("count").toString());
			map.put(serie, numBooks);
		}

		numBooksBySerieCache.put(key, new CachedNumBooksBySerie(map, System.currentTimeMillis()));
		return map;
	}

	@Override
	public Long getNumSeries(List<String> languages) {
		String key = languages == null ? "" : String.join(",", languages);
		CachedNumSeries cached = numSeriesCache.get(key);
		if (cached != null && !cached.isExpired()) {
			return cached.count();
		}

		Long ret = 0L;
		List<Document> list = Arrays.asList(
				new Document("$match", buildSerieMatchDocument(languages)),
				new Document("$project", new Document("serie.name", 1L)),
				new Document("$group", new Document("_id", "$serie.name").append("count", new Document("$sum", 1L))),
				new Document("$count", "count"));

		AggregateIterable<Document> data = mongoTemplate.getCollection(collectionName).aggregate(list);
		if (data.iterator().hasNext()) {
			ret = Long.parseLong(data.iterator().next().get("count").toString());
		}

		numSeriesCache.put(key, new CachedNumSeries(ret, System.currentTimeMillis()));
		return ret;
	}

	@Override
	public SeriePageData getSeriesPage(List<String> languages, int page, int size, String sort, String order) {
		String key = (languages == null ? "" : String.join(",", languages)) + ":" + page + ":" + size + ":" + sort + ":" + order;
		CachedSeriesPage cached = seriesPageCache.get(key);
		if (cached != null && !cached.isExpired()) {
			return cached.data();
		}

		Map<String, Long> items = new LinkedHashMap<>();

		List<Document> list = Arrays.asList(
				new Document("$match", buildSerieMatchDocument(languages)),
				new Document("$project", new Document("serie.name", 1L)),
				new Document("$group", new Document("_id", "$serie.name").append("count", new Document("$sum", 1L))),
				new Document("$facet", new Document("items", Arrays.asList(
						new Document("$sort", new Document("numBooks".equals(sort) ? "count" : "_id", "asc".equalsIgnoreCase(order) ? 1 : -1)),
						new Document("$skip", page * size),
						new Document("$limit", size)
				)).append("total", Arrays.asList(
						new Document("$count", "count")
				)))
		);

		Document facet = mongoTemplate.getCollection(collectionName).aggregate(list).first();
		if (facet == null) {
			SeriePageData empty = new SeriePageData(items, 0L);
			seriesPageCache.put(key, new CachedSeriesPage(empty, System.currentTimeMillis()));
			return empty;
		}

		List<Document> itemDocuments = facet.getList("items", Document.class, Collections.emptyList());
		for (Document document : itemDocuments) {
			String serie = String.valueOf(document.get("_id"));
			Long numBooks = Long.parseLong(String.valueOf(document.get("count")));
			items.put(serie, numBooks);
		}

		List<Document> totalDocuments = facet.getList("total", Document.class, Collections.emptyList());
		long total = totalDocuments.isEmpty() ? 0L : Long.parseLong(String.valueOf(totalDocuments.get(0).get("count")));

		SeriePageData result = new SeriePageData(items, total);
		seriesPageCache.put(key, new CachedSeriesPage(result, System.currentTimeMillis()));
		return result;
	}

	@Override
	public Optional<String> findFirstImageBySerie(String serie) {
		if (StringUtils.isBlank(serie)) {
			return Optional.empty();
		}

		Query query = new Query()
				.addCriteria(Criteria.where("serie.name").is(serie))
				.with(Sort.by(Sort.Direction.ASC, "serie.index").and(Sort.by(Sort.Direction.ASC, "_id")))
				.limit(1);
		query.fields().include("image");

		BookMongoEntity book = mongoTemplate.findOne(query, BookMongoEntity.class);
		return Optional.ofNullable(book)
				.map(BookMongoEntity::getImage)
				.filter(StringUtils::isNotBlank);
	}

	private Query buildSearchQuery(Search search) {
		Query query = new Query();

		List<Criteria> criterias = new ArrayList<>();

		if (search != null && !search.isEmpty()) {
			if (StringUtils.isNoneEmpty(search.getPath())) {
				String path = StringUtils.stripAccents(search.getPath());
				criterias.add(Criteria.where("path").regex(path, "i"));
			}

			if (StringUtils.isNotBlank(search.getTitle())) {
				criterias.add(Criteria.where("title").regex(Pattern.quote(search.getTitle()), "i"));
			}

			if (StringUtils.isNotBlank(search.getAuthor())) {
				criterias.add(Criteria.where("authors").regex(Pattern.quote(search.getAuthor()), "i"));
			}

			if (search.getIni() != null) {
				criterias.add(Criteria.where("pubDate").gte(search.getIni()));
			}

			if (search.getEnd() != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(search.getEnd());
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);
				criterias.add(Criteria.where("pubDate").lte(c.getTime()));
			}

			if (search.getMin() != null) {
				criterias.add(Criteria.where("pages").gte(search.getMin()));
			}

			if (search.getMax() != null) {
				criterias.add(Criteria.where("pages").lte(search.getMax()));
			}

			if (!CollectionUtils.isEmpty(search.getSelectedTags())) {
				criterias.add(Criteria.where("tags").in(search.getSelectedTags()));
			}

			if (StringUtils.isNotBlank(search.getSerie())) {
				criterias.add(Criteria.where("serie.name").regex(Pattern.quote(search.getSerie()), "i"));
			}
		}

		if (search != null) {
			if (!CollectionUtils.isEmpty(search.getLanguages())) {
				criterias.add(Criteria.where("languages").in(LanguageCodeUtils.expand(search.getLanguages())));
			}
			if (!criterias.isEmpty()) {
				query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[0])));
			}
		}

		return query;
	}

	private void excludeHeavyBookFields(Query query) {
		query.fields()
				.exclude("reviews")
				.exclude("similar")
				.exclude("recommendations");
	}

	private Bson buildSearchMatchDocument(Search search) {
		Query query = buildSearchQuery(search);
		Document document = query.getQueryObject();
		return document.isEmpty() ? null : new Document("$match", document);
	}

	private Book mapEntityToDomain(BookMongoEntity entity) {
		return bookMongoMapper.entity2Domain(entity);
	}

	@Override
	public List<BookMongoEntity> getSerie(String serie, List<String> languages) {
		Query query = new Query();

		List<Criteria> criterias = new ArrayList<>();

		criterias.add(Criteria.where("serie.name").is(serie));

		if (!CollectionUtils.isEmpty(languages)) {
			criterias.add(Criteria.where("languages").in(LanguageCodeUtils.expand(languages)));
		}
		query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));

		return mongoTemplate.find(query, BookMongoEntity.class);
	}

	@Override
	public List<BookMongoEntity> getSimilar(List<String> similar, List<String> languages) {
		if (CollectionUtils.isEmpty(similar)) {
			return Collections.emptyList();
		}

		List<ObjectId> ids = similar.stream()
				.filter(id -> id != null && ObjectId.isValid(id))
				.distinct()
				.map(ObjectId::new)
				.collect(Collectors.toList());

		if (ids.isEmpty()) {
			return Collections.emptyList();
		}

		Bson idFilter = Filters.in("_id", ids);
		Bson filter = CollectionUtils.isEmpty(languages)
				? idFilter
				: Filters.and(idFilter, Filters.in("languages", LanguageCodeUtils.expand(languages)));

		CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(
				MongoClientSettings.getDefaultCodecRegistry(),
				org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		List<BookMongoEntity> result = new ArrayList<>(ids.size());
		FindIterable<BookMongoEntity> data = mongoTemplate.getCollection(collectionName)
				.withCodecRegistry(pojoCodecRegistry)
				.find(filter, BookMongoEntity.class);

		data.iterator().forEachRemaining(result::add);
		Collections.shuffle(result);
		return result;
	}

	@Override
	public List<BookMongoEntity> getRecommendationsByBook(List<String> recommendations, List<String> languages, int num) {
		if (CollectionUtils.isEmpty(recommendations) || num <= 0) {
			return Collections.emptyList();
		}

		List<ObjectId> ids = recommendations.stream()
				.filter(Objects::nonNull)
				.map(ObjectId::new)
				.collect(Collectors.toList());

		if (ids.isEmpty()) {
			return Collections.emptyList();
		}

		Bson idFilter = Filters.in("_id", ids);
		Bson filter = CollectionUtils.isEmpty(languages)
				? idFilter
				: Filters.and(idFilter, Filters.in("languages", LanguageCodeUtils.expand(languages)));

		CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(
				MongoClientSettings.getDefaultCodecRegistry(),
				org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		List<BookMongoEntity> result = new ArrayList<>(Math.min(ids.size(), num));
		FindIterable<BookMongoEntity> books = mongoTemplate.getCollection(collectionName)
				.withCodecRegistry(pojoCodecRegistry)
				.find(filter, BookMongoEntity.class);

		books.iterator().forEachRemaining(result::add);
		Collections.shuffle(result);
		if (result.size() > num) {
			return new ArrayList<>(result.subList(0, num));
		}
		return result;
	}

	@Resource
	private UserRecommendationQuery userRecommendationQuery;

	@Override
	public long countRecommendationsByUser(String user) {
		return userRecommendationQuery.count(user);
	}

	@Override
	public List<BookMongoEntity> getRecommendationsByUser(String user, int page, int size, String sort, String order) {
		return userRecommendationQuery.page(user, page, size, sort, order, false).items();
	}

	@Override
	public BookPageData getRecommendationSummaryPage(String user, int page, int size, String sort, String order) {
		var result = userRecommendationQuery.page(user, page, size, sort, order, true);
		return new BookPageData(bookMongoMapper.entities2Domains(result.items()), result.total());
	}

	private String resolveSortField(String sort) {
		if (!ALLOWED_SORT_FIELDS.contains(sort)) {
			return "title";
		}
		return "id".equals(sort) ? "_id" : sort;
	}

	private Direction resolveDirection(String order) {
		return "desc".equalsIgnoreCase(order) ? Direction.DESC : Direction.ASC;
	}

	@Override
	public List<String> getBookLanguages() {
		return LanguageCodeUtils.normalizeAll(mongoTemplate.findDistinct(new Query(), "languages", BookMongoEntity.class, String.class));
	}

	@Override
	public void clearCache() {
		seriesPageCache.clear();
		numSeriesCache.clear();
		numBooksBySerieCache.clear();
	}

}
