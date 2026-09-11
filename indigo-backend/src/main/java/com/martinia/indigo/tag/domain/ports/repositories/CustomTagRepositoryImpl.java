package com.martinia.indigo.tag.domain.ports.repositories;

import java.util.ArrayList;
import java.util.List;

import com.martinia.indigo.common.util.LanguageCodeUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;

import jakarta.annotation.Resource;

@Repository
public class CustomTagRepositoryImpl implements CustomTagRepository {

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public List<TagMongoEntity> findAll(List<String> languages, Sort sort) {
		Query query = new Query().with(Sort.by(sort.stream().toList()));
		List<Criteria> criterias = new ArrayList<>();
		if (!CollectionUtils.isEmpty(languages)) {
			for (String lang : languages.stream()
					.flatMap(language -> LanguageCodeUtils.variants(language).stream()).distinct().toList())
				criterias.add(Criteria.where("numBooks.languages." + lang)
						.exists(true));
			query.addCriteria(new Criteria().orOperator(criterias.toArray(new Criteria[criterias.size()])));
		}

		return mongoTemplate.find(query, TagMongoEntity.class);
	}

	@Override
	public List<TagMongoEntity> findAll(List<String> languages, int page, int size, Sort sort) {
		Query query = new Query().with(PageRequest.of(page, size, sort));
		List<Criteria> criterias = new ArrayList<>();
		if (!CollectionUtils.isEmpty(languages)) {
			for (String lang : languages.stream()
					.flatMap(language -> LanguageCodeUtils.variants(language).stream()).distinct().toList())
				criterias.add(Criteria.where("numBooks.languages." + lang)
						.exists(true));
			query.addCriteria(new Criteria().orOperator(criterias.toArray(new Criteria[criterias.size()])));
		}

		return mongoTemplate.find(query, TagMongoEntity.class);
	}

}
