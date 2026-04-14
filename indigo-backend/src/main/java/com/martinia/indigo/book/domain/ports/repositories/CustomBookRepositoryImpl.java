package com.martinia.indigo.book.domain.ports.repositories;

import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.domain.model.Search;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonNull;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
import java.util.stream.Collectors;

@Repository
@Slf4j
public class CustomBookRepositoryImpl implements CustomBookRepository {

	private static final Set<String> ALLOWED_SORT_FIELDS = new LinkedHashSet<>(Arrays.asList(
			"title", "path", "pubDate", "pages", "rating", "lastModified", "_id", "id"
	));

	@Resource
	private MongoTemplate mongoTemplate;

	@Resource
	private UserRepository userRepository;

	@Resource
	private NotificationRepository notificationRepository;

	private String collectionName = BookMongoEntity.class.getAnnotation(org.springframework.data.mongodb.core.mapping.Document.class)
			.collection();

	public long countBooks(Search search) {

		        Query query = new Query();
		
		        List<Criteria> criterias = new ArrayList<>();
		
		        if (search != null && !search.isEmpty()) {
		
		            if (StringUtils.isNoneEmpty(search.getPath())) {
		                String path = StringUtils.stripAccents(search.getPath());
		                criterias.add(Criteria.where("path").regex(path, "i"));
		            }
		
		            if (StringUtils.isNoneEmpty(search.getTitle()) || StringUtils.isNoneEmpty(search.getAuthor())) {
		                String textToSearch = (Optional.ofNullable(search.getTitle()).orElse("") + " " + Optional.ofNullable(search.getAuthor()).orElse("")).trim();
		                if (StringUtils.isNotBlank(textToSearch)) {
		                    query.addCriteria(TextCriteria.forDefaultLanguage().matchingAny(textToSearch));
		                }
		            }
			if (null != (search.getIni())) {
				criterias.add(Criteria.where("pubDate").gte(search.getIni()));
			}

			if (null != (search.getEnd())) {

				Calendar c = Calendar.getInstance();
				c.setTime(search.getEnd());
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);

				criterias.add(Criteria.where("pubDate").lte(c.getTime()));

			}

			if (null != (search.getMin())) {
				criterias.add(Criteria.where("pages").gte(search.getMin()));
			}

			if (null != (search.getMax())) {
				criterias.add(Criteria.where("pages").lte(search.getMax()));
			}

			if (!CollectionUtils.isEmpty(search.getSelectedTags())) {
				criterias.add(Criteria.where("tags").in(search.getSelectedTags()));
			}

			if (StringUtils.isNoneEmpty(search.getSerie())) {
				criterias.add(Criteria.where("serie.name").is(search.getSerie()));
			}

		}

		        if (search != null) {//TODO: mejorar esto
		            if (!CollectionUtils.isEmpty(search.getLanguages())) {
		                criterias.add(Criteria.where("languages").in(search.getLanguages()));
		            }
		            if (!criterias.isEmpty()) {
		                query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));
		            }
		        }
		return mongoTemplate.count(query, BookMongoEntity.class);
	}

	public List<BookMongoEntity> findAll(Search search, int page, int size, String sort, String order) {

		Query query = new Query().with(PageRequest.of(page, size, Sort.by(resolveDirection(order), resolveSortField(sort))));
		query.fields()
				.exclude("reviews")
				.exclude("similar")
				.exclude("recommendations");

		List<Criteria> criterias = new ArrayList<>();

		if (search != null && !search.isEmpty()) {

			if (StringUtils.isNoneEmpty(search.getPath())) {

				String path = StringUtils.stripAccents(search.getPath());
				criterias.add(Criteria.where("path").regex(path, "i"));
			}

			if (StringUtils.isNoneEmpty(search.getTitle()) || StringUtils.isNoneEmpty(search.getAuthor())) {
				String textToSearch = (Optional.ofNullable(search.getTitle()).orElse("") + " " + Optional.ofNullable(search.getAuthor()).orElse("")).trim();
				if (StringUtils.isNotBlank(textToSearch)) {
					query.addCriteria(TextCriteria.forDefaultLanguage().matchingAny(textToSearch));
				}
			}

			if (null != (search.getIni())) {
				criterias.add(Criteria.where("pubDate").gte(search.getIni()));
			}

			if (null != (search.getEnd())) {

				Calendar c = Calendar.getInstance();
				c.setTime(search.getEnd());
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);

				criterias.add(Criteria.where("pubDate").lte(c.getTime()));

			}

			if (null != (search.getMin())) {
				criterias.add(Criteria.where("pages").gte(search.getMin()));
			}

			if (null != (search.getMax())) {
				criterias.add(Criteria.where("pages").lte(search.getMax()));
			}

			if (!CollectionUtils.isEmpty(search.getSelectedTags())) {
				criterias.add(Criteria.where("tags").in(search.getSelectedTags()));
			}

			if (StringUtils.isNoneEmpty(search.getSerie())) {
				criterias.add(Criteria.where("serie.name").is(search.getSerie()));
			}

		}

		if (search != null) {//TODO: mejorar esto
			if (!CollectionUtils.isEmpty(search.getLanguages())) {
				criterias.add(Criteria.where("languages").in(search.getLanguages()));
			}
			if (!criterias.isEmpty()) {
				query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));
			}
		}

		return mongoTemplate.find(query, BookMongoEntity.class);

	}

	@Override
	public List<BookMongoEntity> getRecommendationsByBook(BookMongoEntity book) {

		try {
			Query query = new Query();

			List<Criteria> criterias = new ArrayList<>();

			criterias.add(Criteria.where("id").ne(book.getId()));

			criterias.add(Criteria.where("tags").all(book.getTags()));

			Optional.ofNullable(book.getPubDate()).ifPresent(pubDate -> {
				Calendar cIni = Calendar.getInstance();
				cIni.setTime(pubDate);
				cIni.add(Calendar.YEAR, -5);

				criterias.add(Criteria.where("pubDate").gte(cIni.getTime()));

				Calendar cEnd = Calendar.getInstance();
				cEnd.setTime(book.getPubDate());
				cEnd.set(Calendar.HOUR_OF_DAY, 23);
				cEnd.set(Calendar.MINUTE, 59);
				cEnd.add(Calendar.YEAR, 5);

				criterias.add(Criteria.where("pubDate").lte(cEnd.getTime()));

			});

			criterias.add(Criteria.where("pages").gte(book.getPages() - ((book.getPages() * 25) / 100)));

			criterias.add(Criteria.where("pages").lte(book.getPages() + ((book.getPages() * 25) / 100)));

			query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));

			return mongoTemplate.find(query, BookMongoEntity.class);
		}
		catch (Exception e) {
			log.error(e.getMessage());
			return Collections.emptyList();
		}

	}

	@Override
	public Map<String, Long> getNumBooksBySerie(List<String> languages, int page, int size, String sort, String order) {

		Map<String, Long> map = new LinkedHashMap<>();

		List<Document> list = Arrays.asList(new Document("$match",
						new Document("serie.name", new Document("$ne", new BsonNull())).append("languages", new Document("$in", languages))),
				new Document("$project", new Document("serie.name", 1L)),
				new Document("$group", new Document("_id", "$serie.name").append("count", new Document("$sum", 1L))),
				new Document("$sort", new Document(sort.equals("numBooks") ? "count" : "_id", order.equalsIgnoreCase("asc") ? 1 : -1)),
				new Document("$skip", page * size), new Document("$limit", size));

		AggregateIterable<Document> data = mongoTemplate.getCollection(collectionName).aggregate(list);

		Iterator<Document> it = data.iterator();
		while (it.hasNext()) {
			Document document = it.next();
			String serie = document.get("_id").toString();
			Long numBooks = Long.parseLong(document.get("count").toString());
			map.put(serie, numBooks);
		}

		return map;

	}

	@Override
	public Long getNumSeries(List<String> languages) {
		Long ret = 0L;
		List<Document> list = Arrays.asList(new Document("$match",
						new Document("serie.name", new Document("$ne", new BsonNull())).append("languages", new Document("$in", languages))),
				new Document("$project", new Document("serie.name", 1L)),
				new Document("$group", new Document("_id", "$serie.name").append("count", new Document("$sum", 1L))),
				new Document("$count", "count"));

		AggregateIterable<Document> data = mongoTemplate.getCollection(collectionName).aggregate(list);
		if (data.iterator().hasNext()) {
			ret = Long.parseLong(data.iterator().next().get("count").toString());
		}

		return ret;
	}

	@Override
	public List<BookMongoEntity> getSerie(String serie, List<String> languages) {
		Query query = new Query();

		List<Criteria> criterias = new ArrayList<>();

		criterias.add(Criteria.where("serie.name").is(serie));

		if (!CollectionUtils.isEmpty(languages)) {
			criterias.add(Criteria.where("languages").in(languages));
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
				.filter(Objects::nonNull)
				.map(ObjectId::new)
				.collect(Collectors.toList());

		if (ids.isEmpty()) {
			return Collections.emptyList();
		}

		Bson idFilter = Filters.in("_id", ids);
		Bson filter = CollectionUtils.isEmpty(languages)
				? idFilter
				: Filters.and(idFilter, Filters.in("languages", languages));

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
				: Filters.and(idFilter, Filters.in("languages", languages));

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

	@Override
	public long countRecommendationsByUser(String user) {
		Optional<UserMongoEntity> userMongoEntity = userRepository.findByUsername(user);
		if (userMongoEntity.isEmpty()) {
			return 0;
		}

		List<NotificationMongoEntity> notifications = notificationRepository.findByUserAndType(user, "KINDLE");
		if (CollectionUtils.isEmpty(notifications)) {
			return 0;
		}

		List<String> sentBookPaths = notifications.stream()
				.map(NotificationMongoEntity::getKindle)
				.filter(Objects::nonNull)
				.map(notification -> notification.getBook())
				.filter(Objects::nonNull)
				.distinct()
				.toList();

		if (CollectionUtils.isEmpty(sentBookPaths)) {
			return 0;
		}

		List<String> recommendations = mongoTemplate.find(new Query(Criteria.where("path").in(sentBookPaths)), BookMongoEntity.class)
				.stream()
				.map(BookMongoEntity::getRecommendations)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

		if (CollectionUtils.isEmpty(recommendations)) {
			return 0;
		}

		return getRecommendationsByBook(recommendations, userMongoEntity.get().getLanguageBooks(), recommendations.size()).size();
	}

	@Override
	public List<BookMongoEntity> getRecommendationsByUser(String user, int page, int size, String sort, String order) {

		List<BookMongoEntity> ret = new ArrayList<>();
		Optional<UserMongoEntity> userMongoEntity = userRepository.findByUsername(user);
		if (userMongoEntity.isEmpty()) {
			return ret;
		}
		List<String> languages = userMongoEntity.get().getLanguageBooks();

		CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(
				MongoClientSettings.getDefaultCodecRegistry(),
				org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		MongoCollection<Document> collection = mongoTemplate.getCollection("notifications").withCodecRegistry(pojoCodecRegistry);

		AggregateIterable<BookMongoEntity> data = collection.aggregate(Arrays.asList(new Document("$match", new Document("user", user).append("type", "KINDLE")),
				new Document("$project", new Document("_id", 0L).append("book", "$kindle.book")), new Document("$lookup",
						new Document("from", collectionName).append("localField", "book")
								.append("foreignField", "path")
								.append("as", "typeCategory")),
				new Document("$match", new Document("typeCategory.recommendations", new Document("$ne", new BsonNull()))),
				new Document("$unwind", new Document("path", "$typeCategory")),
				new Document("$unwind", new Document("path", "$typeCategory.recommendations")),
				new Document("$project", new Document("_id", new Document("$toObjectId", "$typeCategory.recommendations"))),
				new Document("$group", new Document("_id", "$_id").append("count", new Document("$sum", 1L))),
				new Document("$sort", new Document("count", -1L)), new Document("$lookup",
						new Document("from", collectionName).append("localField", "_id")
								.append("foreignField", "_id")
								.append("as", "book")), new Document("$replaceRoot", new Document("newRoot",
						new Document("$mergeObjects", Arrays.asList(new Document("$arrayElemAt", Arrays.asList("$book", 0L)), "$$ROOT")))),
				new Document("$match", new Document("languages", new Document("$in", languages))),
				new Document("$sort", new Document(resolveSortField(sort), (resolveDirection(order).isAscending() ? 1 : -1)).append("_id", -1L)),
				new Document("$skip", page * size), new Document("$limit", size)), BookMongoEntity.class);

		data.iterator().forEachRemaining(ret::add);

		return ret;
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
		List<String> ret = new ArrayList<>();

		CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(
				MongoClientSettings.getDefaultCodecRegistry(),
				org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName).withCodecRegistry(pojoCodecRegistry);

		AggregateIterable<Document> data = collection.aggregate(Arrays.asList(new Document("$project", new Document("languages", 1L)),
				new Document("$unwind", new Document("path", "$languages")),
				new Document("$group", new Document("_id", "null").append("languages", new Document("$addToSet", "$languages"))),
				new Document("$unwind", new Document("path", "$languages")), new Document("$project", new Document("_id", 0L))));

		Iterator<Document> it = data.iterator();
		while (it.hasNext()) {
			Document document = it.next();
			ret.add(document.get("languages").toString());
		}

		return ret;
	}

}
