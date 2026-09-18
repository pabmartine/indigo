package com.martinia.indigo.tag.domain.ports.repositories;

import java.util.ArrayList;
import java.util.List;

import com.martinia.indigo.common.util.LanguageCodeUtils;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.model.TagPageData;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import com.martinia.indigo.tag.infrastructure.mongo.mappers.TagMongoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;

@Repository
@Slf4j
public class CustomTagRepositoryImpl implements CustomTagRepository {

	@Resource
	private MongoTemplate mongoTemplate;

	@Resource
	private TagMongoMapper tagMongoMapper;

	private Query buildLanguageQuery(List<String> languages) {
		Query query = new Query();
		List<Criteria> criterias = new ArrayList<>();
		if (!CollectionUtils.isEmpty(languages)) {
			for (String lang : languages.stream()
					.flatMap(language -> LanguageCodeUtils.variants(language).stream()).distinct().toList())
				criterias.add(Criteria.where("numBooks.languages." + lang)
						.exists(true));
			query.addCriteria(new Criteria().orOperator(criterias.toArray(new Criteria[criterias.size()])));
		}
		return query;
	}

	@Override
	public long count(List<String> languages) {
		Query query = buildLanguageQuery(languages);
		return mongoTemplate.count(query, TagMongoEntity.class);
	}

	@Override
	public List<TagMongoEntity> findAll(List<String> languages, Sort sort) {
		Query query = buildLanguageQuery(languages).with(Sort.by(sort.stream().toList()));
		return mongoTemplate.find(query, TagMongoEntity.class);
	}

	@Override
	public List<TagMongoEntity> findAll(List<String> languages, int page, int size, Sort sort) {
		Query query = buildLanguageQuery(languages).with(PageRequest.of(page, size, sort));
		return mongoTemplate.find(query, TagMongoEntity.class);
	}

	@Override
	public List<TagMongoEntity> findSummary(List<String> languages, Pageable page) {
		Query query = buildLanguageQuery(languages).with(page);
		query.fields().include("_id").include("name").include("numBooks");
		return mongoTemplate.find(query, TagMongoEntity.class);
	}

	@Override
	public TagPageData findSummaryPage(List<String> languages, Pageable page) {
		long started = System.nanoTime();
		List<TagMongoEntity> entities = findSummary(languages, page);
		long queried = System.nanoTime();
		long total = count(languages);
		long counted = System.nanoTime();

		entities.forEach(tag -> {
			if (tag.getNumBooks() == null) {
				return;
			}
			if (languages == null || languages.isEmpty()) {
				if (tag.getNumBooks().getLanguages() != null) {
					tag.getNumBooks().setTotal(tag.getNumBooks().getLanguages().values().stream().mapToInt(Integer::intValue).sum());
				}
				return;
			}
			int langTotal = 0;
			if (tag.getNumBooks().getLanguages() != null) {
				for (String key : tag.getNumBooks().getLanguages().keySet()) {
					if (languages.stream().anyMatch(language -> LanguageCodeUtils.variants(language).contains(key))) {
						langTotal += tag.getNumBooks().getLanguages().get(key);
					}
				}
			}
			tag.getNumBooks().setTotal(langTotal);
		});

		List<Tag> items = tagMongoMapper.entities2Domains(entities);
		long finished = System.nanoTime();

		var timingLog = finished - started >= 1_000_000_000L ? log.atInfo() : log.atDebug();
		timingLog.log("Tag summary page={} size={} items={} total={} queryMs={} countMs={} mappingMs={} totalMs={}",
				page.getPageNumber(), page.getPageSize(), items.size(), total, (queried - started) / 1_000_000L,
				(counted - queried) / 1_000_000L, (finished - counted) / 1_000_000L,
				(finished - started) / 1_000_000L);

		return new TagPageData(items, total, page.getPageNumber(), page.getPageSize());
	}

}
