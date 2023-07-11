package com.martinia.indigo.adapters.out.mongo.repository.custom;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;

import javax.annotation.Resource;

@Repository
public class CustomTagMongoRepositoryImpl implements CustomTagMongoRepository {

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public List<TagMongoEntity> findAll(List<String> languages, Sort sort) {
		Query query = new Query().with(PageRequest.of(0, Integer.MAX_VALUE, sort));
		List<Criteria> criterias = new ArrayList<>();
		if (!CollectionUtils.isEmpty(languages)) {
			for (String lang : languages)
				criterias.add(Criteria.where("numBooks.languages." + lang)
						.exists(true));
		}
		query.addCriteria(new Criteria().orOperator(criterias.toArray(new Criteria[criterias.size()])));

		return mongoTemplate.find(query, TagMongoEntity.class);
	}

}
