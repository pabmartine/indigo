package com.martinia.indigo.adapters.out.mongo.repository.custom;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import com.martinia.indigo.adapters.out.mongo.entities.AuthorMongoEntity;

public class CustomAuthorMongoRepositoryImpl implements CustomAuthorMongoRepository {

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public long count(List<String> languages) {
		Query query = new Query();
		List<Criteria> criterias = new ArrayList<>();
		if (!CollectionUtils.isEmpty(languages)) {
			for (String lang : languages)
				criterias.add(Criteria.where("numBooks.languages." + lang)
						.exists(true));
		}
		query.addCriteria(new Criteria().orOperator(criterias.toArray(new Criteria[criterias.size()])));

		return mongoTemplate.count(query, AuthorMongoEntity.class);
	}

	@Override
	public List<AuthorMongoEntity> findAll(List<String> languages, Pageable page) {
		Query query = new Query().with(page);
		List<Criteria> criterias = new ArrayList<>();
		if (!CollectionUtils.isEmpty(languages)) {
			for (String lang : languages)
				criterias.add(Criteria.where("numBooks.languages." + lang)
						.exists(true));
		}
		query.addCriteria(new Criteria().orOperator(criterias.toArray(new Criteria[criterias.size()])));

		return mongoTemplate.find(query, AuthorMongoEntity.class);
	}



}
