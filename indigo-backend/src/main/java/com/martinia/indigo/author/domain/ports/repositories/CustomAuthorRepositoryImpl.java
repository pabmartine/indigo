package com.martinia.indigo.author.domain.ports.repositories;

import java.util.ArrayList;
import java.util.List;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.model.AuthorPageData;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import com.martinia.indigo.common.util.LanguageCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;

@Repository
@Slf4j
public class CustomAuthorRepositoryImpl implements CustomAuthorRepository {

	@Resource
	private MongoTemplate mongoTemplate;

	@Resource
	private AuthorMongoMapper authorMongoMapper;

	private Query buildLanguageQuery(List<String> languages) {
		Query query = new Query();
		List<Criteria> criterias = new ArrayList<>();
		if (!CollectionUtils.isEmpty(languages)) {
			for (String lang : languages.stream().flatMap(language -> LanguageCodeUtils.variants(language).stream()).distinct().toList())
				criterias.add(Criteria.where("numBooks.languages." + lang)
						.exists(true));
			query.addCriteria(new Criteria().orOperator(criterias.toArray(new Criteria[criterias.size()])));
		}
		return query;
	}

	@Override
	public long count(List<String> languages) {
		Query query = buildLanguageQuery(languages);
		return mongoTemplate.count(query, AuthorMongoEntity.class);
	}

	@Override
	public List<AuthorMongoEntity> findAll(List<String> languages, Pageable page) {
		Query query = buildLanguageQuery(languages).with(page);
		return mongoTemplate.find(query, AuthorMongoEntity.class);
	}

	@Override
	public List<AuthorMongoEntity> findSummary(List<String> languages, Pageable page) {
		Query query = buildLanguageQuery(languages).with(page);
		query.fields().include("_id").include("name").include("sort").include("numBooks");
		return mongoTemplate.find(query, AuthorMongoEntity.class);
	}

	@Override
	public AuthorPageData findSummaryPage(List<String> languages, Pageable page) {
		long started = System.nanoTime();
		List<AuthorMongoEntity> entities = findSummary(languages, page);
		long queried = System.nanoTime();
		long total = count(languages);
		long counted = System.nanoTime();
		List<Author> items = authorMongoMapper.entities2Domains(entities);
		long finished = System.nanoTime();
		var timingLog = finished - started >= 1_000_000_000L ? log.atInfo() : log.atDebug();
		timingLog.log("Author summary page={} size={} items={} total={} queryMs={} countMs={} mappingMs={} totalMs={}",
				page.getPageNumber(), page.getPageSize(), items.size(), total, (queried - started) / 1_000_000L,
				(counted - queried) / 1_000_000L, (finished - counted) / 1_000_000L,
				(finished - started) / 1_000_000L);
		return new AuthorPageData(items, total);
	}

}
