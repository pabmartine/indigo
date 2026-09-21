package com.martinia.indigo.tag.domain.ports.repositories;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class CustomTagRepositoryImpl implements CustomTagRepository {

	private static final long COUNT_CACHE_TTL_MS = 5 * 60 * 1000L;
	private final ConcurrentMap<String, CachedCount> countCache = new ConcurrentHashMap<>();

	private record CachedCount(long count, long timestamp) {
		boolean isExpired() {
			return System.currentTimeMillis() - timestamp > COUNT_CACHE_TTL_MS;
		}
	}

	@Resource
	private MongoTemplate mongoTemplate;

	@Resource
	private TagMongoMapper tagMongoMapper;

	private Query buildLanguageQuery(List<String> languages) {
		Query query = new Query();
		if (!CollectionUtils.isEmpty(languages)) {
			List<Criteria> criterias = languages.stream()
					.flatMap(language -> LanguageCodeUtils.variants(language).stream())
					.distinct()
					.map(lang -> Criteria.where("numBooks.languages." + lang).gt(0))
					.toList();
			if (criterias.size() == 1) {
				query.addCriteria(criterias.get(0));
			}
			else if (criterias.size() > 1) {
				query.addCriteria(new Criteria().orOperator(criterias.toArray(new Criteria[0])));
			}
		}
		return query;
	}

	@Override
	public long count(List<String> languages) {
		String key = languages == null || languages.isEmpty() ? "" : languages.stream().sorted().collect(Collectors.joining(","));
		CachedCount cached = countCache.get(key);
		if (cached != null && !cached.isExpired()) {
			return cached.count();
		}
		Query query = buildLanguageQuery(languages);
		if (query.getQueryObject().isEmpty()) {
			// Exact count using the always-present _id index, without reading image-heavy documents.
			query.withHint("_id_");
		}
		long total = mongoTemplate.count(query, TagMongoEntity.class);
		countCache.put(key, new CachedCount(total, System.currentTimeMillis()));
		return total;
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
		// Infer the total only when this is provably the last page.
		long total = entities.size() < page.getPageSize() && (page.getOffset() == 0 || !entities.isEmpty())
				? page.getOffset() + entities.size() : count(languages);
		long counted = System.nanoTime();

		java.util.Set<String> requestedVariants = (languages == null || languages.isEmpty())
				? java.util.Collections.emptySet()
				: languages.stream().flatMap(l -> LanguageCodeUtils.variants(l).stream()).collect(Collectors.toSet());

		entities.forEach(tag -> {
			if (tag.getNumBooks() == null) {
				return;
			}
			if (requestedVariants.isEmpty()) {
				return;
			}
			int langTotal = 0;
			if (tag.getNumBooks().getLanguages() != null) {
				langTotal = tag.getNumBooks().getLanguages().entrySet().stream()
						.filter(e -> requestedVariants.contains(e.getKey()))
						.mapToInt(java.util.Map.Entry::getValue).sum();
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

	@Override
	public void clearCache() {
		countCache.clear();
	}

}
