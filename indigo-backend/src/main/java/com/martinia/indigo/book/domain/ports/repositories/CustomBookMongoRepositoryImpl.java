package com.martinia.indigo.book.domain.ports.repositories;

import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import com.martinia.indigo.common.model.Search;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
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
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.in;

@Repository
public class CustomBookMongoRepositoryImpl implements CustomBookMongoRepository {

	@Resource
	private MongoTemplate mongoTemplate;

	public long count(Search search) {

		Query query = new Query();

		List<Criteria> criterias = new ArrayList<>();

		if (search != null && !search.isEmpty()) {

			if (StringUtils.isNoneEmpty(search.getPath())) {

				String path = StringUtils.stripAccents(search.getPath());
				String[] terms = path.split(" ");
				for (String term : terms) {
					criterias.add(Criteria.where("path").regex(term, "i"));
				}
			}

			if (StringUtils.isNoneEmpty(search.getTitle())) {
				String title = (search.getTitle());
				String[] terms = title.split(" ");
				for (String term : terms) {
					criterias.add(Criteria.where("title").regex(term, "i"));
				}
			}

			if (StringUtils.isNoneEmpty(search.getAuthor())) {
				String author = (search.getAuthor());
				String[] terms = author.split(" ");
				for (String term : terms) {
					criterias.add(Criteria.where("authors").regex(term, "i"));
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
			else {
				criterias.add(Criteria.where("languages").in(Arrays.asList("none")));
			}
			query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));
		}

		return mongoTemplate.count(query, BookMongoEntity.class);

	}

	public List<BookMongoEntity> findAll(Search search, int page, int size, String sort, String order) {

		Query query = new Query().with(PageRequest.of(page, size, Sort.by(Direction.fromString(order), sort)));

		List<Criteria> criterias = new ArrayList<>();

		if (search != null && !search.isEmpty()) {

			if (StringUtils.isNoneEmpty(search.getPath())) {

				String path = StringUtils.stripAccents(search.getPath());
				String[] terms = path.split(" ");
				for (String term : terms) {
					criterias.add(Criteria.where("path").regex(term, "i"));
				}
			}

			if (StringUtils.isNoneEmpty(search.getTitle())) {
				String title = (search.getTitle());
				String[] terms = title.split(" ");
				for (String term : terms) {
					criterias.add(Criteria.where("title").regex(term, "i"));
				}
			}

			if (StringUtils.isNoneEmpty(search.getAuthor())) {
				String author = (search.getAuthor());
				String[] terms = author.split(" ");
				for (String term : terms) {
					criterias.add(Criteria.where("authors").regex(term, "i"));
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
			else {
				criterias.add(Criteria.where("languages").in(Arrays.asList("none")));
			}
			query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));
		}

		return mongoTemplate.find(query, BookMongoEntity.class);

	}

	@Override
	public List<BookMongoEntity> getRecommendationsByBook(BookMongoEntity book) {

		Query query = new Query();

		List<Criteria> criterias = new ArrayList<>();

		criterias.add(Criteria.where("id").ne(book.getId()));

		criterias.add(Criteria.where("tags").all(book.getTags()));

		Calendar cIni = Calendar.getInstance();
		cIni.setTime(book.getPubDate());
		cIni.add(Calendar.YEAR, -5);

		criterias.add(Criteria.where("pubDate").gte(cIni.getTime()));

		Calendar cEnd = Calendar.getInstance();
		cEnd.setTime(book.getPubDate());
		cEnd.set(Calendar.HOUR_OF_DAY, 23);
		cEnd.set(Calendar.MINUTE, 59);
		cEnd.add(Calendar.YEAR, 5);

		criterias.add(Criteria.where("pubDate").lte(cEnd.getTime()));

		criterias.add(Criteria.where("pages").gte(book.getPages() - ((book.getPages() * 25) / 100)));

		criterias.add(Criteria.where("pages").lte(book.getPages() + ((book.getPages() * 25) / 100)));

		query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));

		return mongoTemplate.find(query, BookMongoEntity.class);
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

		AggregateIterable<Document> data = mongoTemplate.getCollection("books").aggregate(list);

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
		Long ret = null;
		List<Document> list = Arrays.asList(new Document("$match",
						new Document("serie.name", new Document("$ne", new BsonNull())).append("languages", new Document("$in", languages))),
				new Document("$project", new Document("serie.name", 1L)),
				new Document("$group", new Document("_id", "$serie.name").append("count", new Document("$sum", 1L))),
				new Document("$group", new Document("_id", "$serie.name").append("count", new Document("$sum", 1L))));

		AggregateIterable<Document> data = mongoTemplate.getCollection("books").aggregate(list);
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
		else {
			criterias.add(Criteria.where("languages").in(Arrays.asList("none")));
		}
		query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));

		return mongoTemplate.find(query, BookMongoEntity.class);
	}

	@Override
	public List<BookMongoEntity> getSimilar(List<String> similar, List<String> languages) {
		List<BookMongoEntity> ret = new ArrayList<>(similar.size());
		List<ObjectId> list = new ArrayList<>(similar.size());
		for (String s : similar) {
			list.add(new ObjectId(s));
		}
		Bson filter = in("_id", list);

		CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(
				MongoClientSettings.getDefaultCodecRegistry(),
				org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		FindIterable<BookMongoEntity> data = mongoTemplate.getCollection("books").withCodecRegistry(pojoCodecRegistry)
				.find(filter, BookMongoEntity.class);

		data.iterator().forEachRemaining(ret::add);

		ret = ret.stream().filter(b -> !Collections.disjoint(b.getLanguages(), languages)).collect(Collectors.toList());
		Collections.shuffle(ret);
		// if (ret.size() > num)
		// ret = ret.subList(0, num);

		return ret;
	}

	@Override
	public List<BookMongoEntity> getRecommendationsByBook(List<String> recommendations, List<String> languages, int num) {

		CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(
				MongoClientSettings.getDefaultCodecRegistry(),
				org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		List<BookMongoEntity> ret = new ArrayList<>(recommendations.size());
		List<ObjectId> list = new ArrayList<>(recommendations.size());
		for (String s : recommendations) {
			list.add(new ObjectId(s));
		}
		Bson filter = in("_id", list);

		FindIterable<BookMongoEntity> books = mongoTemplate.getCollection("books").withCodecRegistry(pojoCodecRegistry)
				.find(filter, BookMongoEntity.class);

		books.iterator().forEachRemaining(ret::add);

		ret = ret.stream().filter(b -> !Collections.disjoint(b.getLanguages(), languages)).collect(Collectors.toList());
		Collections.shuffle(ret);
		if (ret.size() > num) {
			ret = ret.subList(0, num);
		}

		return ret;
	}

	@Override
	public long countRecommendationsByUser(String user) {

		long ret = 0;

		//    long init = System.currentTimeMillis();
		Query query = new Query();
		List<Criteria> criterias = new ArrayList<>();
		criterias.add(Criteria.where("user").is(user));
		query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));
		List<NotificationMongoEntity> notifs = mongoTemplate.find(query, NotificationMongoEntity.class);

		if (!CollectionUtils.isEmpty(notifs)) {

			query = new Query();
			criterias.clear();
			criterias.add(Criteria.where("username").is(user));
			query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));
			UserMongoEntity userMongoEntity = mongoTemplate.findOne(query, UserMongoEntity.class);

			List<String> languages = userMongoEntity.getLanguageBooks();

			List<String> recommendations = new ArrayList<>();

			for (NotificationMongoEntity notif : notifs) {

				query = new Query();
				criterias.clear();
				criterias.add(Criteria.where("path").is(notif.getBook()));
				query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));
				BookMongoEntity book = mongoTemplate.findOne(query, BookMongoEntity.class);

				if (book != null && !CollectionUtils.isEmpty(book.getRecommendations())) {
					recommendations.addAll(book.getRecommendations());
				}

			}

			if (!CollectionUtils.isEmpty(recommendations)) {
				recommendations = recommendations.stream().distinct().collect(Collectors.toList());

				ret = getRecommendationsByBook(recommendations, languages, recommendations.size()).size();

			}

		}
		//    System.out.println(System.currentTimeMillis() - init);
		//    init = System.currentTimeMillis();
		//    Query query = new Query();
		//    List<Criteria> criterias = new ArrayList<>();
		//    criterias.add(Criteria.where("username").is(user));
		//    query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));
		//    UserMongoEntity userMongoEntity = mongoTemplate.findOne(query, UserMongoEntity.class);
		//    List<String> languages = userMongoEntity.getLanguageBooks();
		//
		//    CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(
		//        MongoClientSettings.getDefaultCodecRegistry(),
		//        org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder()
		//            .automatic(true)
		//            .build()));
		//
		//    MongoCollection<Document> collection = mongoTemplate.getCollection("notifications")
		//        .withCodecRegistry(pojoCodecRegistry);
		//
		//    AggregateIterable<Document> data = collection.aggregate(Arrays.asList(
		//
		//        new Document("$match", new Document("user", user)),
		//        new Document("$project", new Document("_id", 0L).append("book", 1L)),
		//        new Document("$lookup", new Document("from", "books").append("localField", "book")
		//            .append("foreignField", "path")
		//            .append("as", "typeCategory")),
		//        new Document("$match",
		//            new Document("typeCategory.recommendations", new Document("$ne", new BsonNull()))),
		//        new Document("$unwind", new Document("path", "$typeCategory")),
		//        new Document("$unwind", new Document("path", "$typeCategory.recommendations")),
		//        new Document("$project",
		//            new Document("_id", new Document("$toObjectId", "$typeCategory.recommendations"))),
		//        new Document("$group", new Document("_id", "$_id").append("count", new Document("$sum", 1L))),
		//        new Document("$sort", new Document("count", -1L)),
		//        new Document("$lookup", new Document("from", "books").append("localField", "_id")
		//            .append("foreignField", "_id")
		//            .append("as", "book")),
		//        new Document("$replaceRoot", new Document("newRoot",
		//            new Document("$mergeObjects",
		//                Arrays.asList(new Document("$arrayElemAt", Arrays.asList("$book", 0L)), "$$ROOT")))),
		//        new Document("$match",
		//            new Document("languages",
		//                new Document("$in", languages))),
		//        new Document("$count", "total")));
		//
		//    if (data.iterator()
		//        .hasNext()) {
		//      ret = Long.parseLong(data.iterator()
		//          .next()
		//          .get("total")
		//          .toString());
		//    }
		//    System.out.println(System.currentTimeMillis() - init);
		return ret;
	}

	@Override
	public List<BookMongoEntity> getRecommendationsByUser(String user, int page, int size, String sort, String order) {

		List<BookMongoEntity> ret = new ArrayList<>();

		Query query = new Query();
		List<Criteria> criterias = new ArrayList<>();
		criterias.add(Criteria.where("username").is(user));
		query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));
		UserMongoEntity userMongoEntity = mongoTemplate.findOne(query, UserMongoEntity.class);
		List<String> languages = userMongoEntity.getLanguageBooks();

		CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(
				MongoClientSettings.getDefaultCodecRegistry(),
				org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		MongoCollection<Document> collection = mongoTemplate.getCollection("notifications").withCodecRegistry(pojoCodecRegistry);

		AggregateIterable<BookMongoEntity> data = collection.aggregate(Arrays.asList(new Document("$match", new Document("user", user)),
				new Document("$project", new Document("_id", 0L).append("book", 1L)), new Document("$lookup",
						new Document("from", "books").append("localField", "book").append("foreignField", "path")
								.append("as", "typeCategory")),
				new Document("$match", new Document("typeCategory.recommendations", new Document("$ne", new BsonNull()))),
				new Document("$unwind", new Document("path", "$typeCategory")),
				new Document("$unwind", new Document("path", "$typeCategory.recommendations")),
				new Document("$project", new Document("_id", new Document("$toObjectId", "$typeCategory.recommendations"))),
				new Document("$group", new Document("_id", "$_id").append("count", new Document("$sum", 1L))),
				new Document("$sort", new Document("count", -1L)), new Document("$lookup",
						new Document("from", "books").append("localField", "_id").append("foreignField", "_id").append("as", "book")),
				new Document("$replaceRoot", new Document("newRoot",
						new Document("$mergeObjects", Arrays.asList(new Document("$arrayElemAt", Arrays.asList("$book", 0L)), "$$ROOT")))),
				new Document("$match", new Document("languages", new Document("$in", languages))),
				new Document("$sort", new Document(sort, (order.equals("asc") ? 1 : -1)).append("_id", -1L)),
				new Document("$skip", page * size), new Document("$limit", size - 1),
				new Document("$unset", Arrays.asList("book", "count"))), BookMongoEntity.class);

		data.iterator().forEachRemaining(ret::add);

		return ret;
	}

	@Override
	public List<String> getBookLanguages() {
		List<String> ret = new ArrayList<>();

		CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(
				MongoClientSettings.getDefaultCodecRegistry(),
				org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		MongoCollection<Document> collection = mongoTemplate.getCollection("books").withCodecRegistry(pojoCodecRegistry);

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
