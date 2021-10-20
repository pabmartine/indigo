package com.martinia.indigo.adapters.out.mongo.repository.custom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.BsonNull;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import com.martinia.indigo.adapters.out.mongo.entities.BookMongoEntity;
import com.martinia.indigo.domain.model.Search;
import com.mongodb.client.AggregateIterable;

public class CustomBookMongoRepositoryImpl implements CustomBookMongoRepository {

	@Autowired
	MongoTemplate mongoTemplate;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	public long count(Search search) {

		Query query = new Query();

		if (search != null) {

			List<Criteria> criterias = new ArrayList<>();

			if (StringUtils.isNoneEmpty(search.getPath())) {

				String path = StringUtils.stripAccents(search.getPath());
				String[] terms = path.split(" ");
				for (String term : terms) {
					criterias.add(Criteria.where("path")
							.regex(term, "i"));
				}
			}

			if (StringUtils.isNoneEmpty(search.getTitle())) {
				String title = (search.getTitle());
				String[] terms = title.split(" ");
				for (String term : terms) {
					criterias.add(Criteria.where("title")
							.regex(term, "i"));
				}
			}

			if (StringUtils.isNoneEmpty(search.getAuthor())) {

				String author = (search.getAuthor());
				String[] terms = author.split(" ");
				for (String term : terms) {
					criterias.add(Criteria.where("authors")
							.regex(term, "i"));
				}
			}

			if (null != (search.getIni())) {
				criterias.add(Criteria.where("pubDate")
						.gte(search.getIni()));
			}

			if (null != (search.getEnd())) {

				Calendar c = Calendar.getInstance();
				c.setTime(search.getEnd());
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);

				criterias.add(Criteria.where("pubDate")
						.lte(c.getTime()));

			}

			if (null != (search.getMin())) {
				criterias.add(Criteria.where("pages")
						.gte(search.getMin()));
			}

			if (null != (search.getMax())) {
				criterias.add(Criteria.where("pages")
						.lte(search.getMax()));
			}

			if (!CollectionUtils.isEmpty(search.getSelectedTags())) {
				criterias.add(Criteria.where("tags")
						.in(search.getSelectedTags()));
			}

			if (StringUtils.isNoneEmpty(search.getSerie())) {
				criterias.add(Criteria.where("serie.name")
						.regex(search.getSerie(), "i"));
			}

			query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));

		}

		return mongoTemplate.count(query, BookMongoEntity.class);

	}

	public List<BookMongoEntity> findAll(Search search, int page, int size, String sort, String order) {

		Query query = new Query().with(PageRequest.of(page, size, Sort.by(Direction.fromString(order), sort)));

		if (search != null) {

			List<Criteria> criterias = new ArrayList<>();

			if (StringUtils.isNoneEmpty(search.getPath())) {

				String path = StringUtils.stripAccents(search.getPath());
				String[] terms = path.split(" ");
				for (String term : terms) {
					criterias.add(Criteria.where("path")
							.regex(term, "i"));
				}
			}

			if (StringUtils.isNoneEmpty(search.getTitle())) {
				String title = (search.getTitle());
				String[] terms = title.split(" ");
				for (String term : terms) {
					criterias.add(Criteria.where("title")
							.regex(term, "i"));
				}
			}

			if (StringUtils.isNoneEmpty(search.getAuthor())) {

				String author = (search.getAuthor());
				String[] terms = author.split(" ");
				for (String term : terms) {
					criterias.add(Criteria.where("authors")
							.regex(term, "i"));
				}
			}

			if (null != (search.getIni())) {
				criterias.add(Criteria.where("pubDate")
						.gte(search.getIni()));
			}

			if (null != (search.getEnd())) {

				Calendar c = Calendar.getInstance();
				c.setTime(search.getEnd());
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);

				criterias.add(Criteria.where("pubDate")
						.lte(c.getTime()));

			}

			if (null != (search.getMin())) {
				criterias.add(Criteria.where("pages")
						.gte(search.getMin()));
			}

			if (null != (search.getMax())) {
				criterias.add(Criteria.where("pages")
						.lte(search.getMax()));
			}

			if (!CollectionUtils.isEmpty(search.getSelectedTags())) {
				criterias.add(Criteria.where("tags")
						.in(search.getSelectedTags()));
			}

			if (StringUtils.isNoneEmpty(search.getSerie())) {
				criterias.add(Criteria.where("serie.name")
						.regex(search.getSerie(), "i"));
			}

			query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));

		}

		return mongoTemplate.find(query, BookMongoEntity.class);

	}

	@Override
	public List<BookMongoEntity> getBookRecommendations(String id, int page, int size, String sort, String order) {

		Query query = new Query()
				.with(PageRequest.of(page, Integer.MAX_VALUE, Sort.by(Direction.fromString(order), sort)));

		List<Criteria> criterias = new ArrayList<>();

		BookMongoEntity book = mongoTemplate.findById(id, BookMongoEntity.class);

		criterias.add(Criteria.where("id")
				.ne(book.getId()));

		criterias.add(Criteria.where("tags")
				.all(book.getTags()));

		Calendar cIni = Calendar.getInstance();
		cIni.setTime(book.getPubDate());
		cIni.add(Calendar.YEAR, -5);

		criterias.add(Criteria.where("pubDate")
				.gte(cIni.getTime()));

		Calendar cEnd = Calendar.getInstance();
		cEnd.setTime(book.getPubDate());
		cEnd.set(Calendar.HOUR_OF_DAY, 23);
		cEnd.set(Calendar.MINUTE, 59);
		cEnd.add(Calendar.YEAR, 5);

		criterias.add(Criteria.where("pubDate")
				.lte(cEnd.getTime()));

		criterias.add(Criteria.where("pages")
				.gte(book.getPages() - ((book.getPages() * 25) / 100)));

		criterias.add(Criteria.where("pages")
				.lte(book.getPages() + ((book.getPages() * 25) / 100)));

		query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()])));

		List<BookMongoEntity> books = mongoTemplate.find(query, BookMongoEntity.class);
		Collections.shuffle(books);

		if (books.size() > size) {
			books = books.subList(0, size);
		}

		return books;

	}

	public Map<String, Long> getNumBooksBySerie(int page, int size, String sort, String order) {

		Map<String, Long> map = new LinkedHashMap<>();

		List<Document> list = Arrays.asList(
				new Document("$match", new Document("serie.name", new Document("$ne", new BsonNull()))),
				new Document("$project", new Document("serie.name", 1L)),
				new Document("$group", new Document("_id", "$serie.name").append("count", new Document("$sum", 1L))),
				new Document("$sort",
						new Document(sort.equals("numBooks") ? "count" : "_id",
								order.equalsIgnoreCase("asc") ? 1 : -1)),
				new Document("$skip", page * size), new Document("$limit", size));

		AggregateIterable<Document> data = mongoTemplate.getCollection("books")
				.aggregate(list);

		Iterator<Document> it = data.iterator();
		while (it.hasNext()) {
			Document document = it.next();
			String serie = document.get("_id")
					.toString();
			Long numBooks = Long.parseLong(document.get("count")
					.toString());
			map.put(serie, numBooks);
		}

		return map;

	}

	@Override
	public Long getNumSeries() {
		Long ret = null;
		List<Document> list = Arrays.asList(
				new Document("$match", new Document("serie.name", new Document("$ne", new BsonNull()))),
				new Document("$project", new Document("serie.name", 1L)),
				new Document("$group", new Document("_id", "$serie.name").append("count", new Document("$sum", 1L))),
				new Document("$group", new Document("_id", "$serie.name").append("count", new Document("$sum", 1L))));

		AggregateIterable<Document> data = mongoTemplate.getCollection("books")
				.aggregate(list);
		if (data.iterator()
				.hasNext()) {
			ret = Long.parseLong(data.iterator()
					.next()
					.get("count")
					.toString());
		}

		return ret;
	}

}
