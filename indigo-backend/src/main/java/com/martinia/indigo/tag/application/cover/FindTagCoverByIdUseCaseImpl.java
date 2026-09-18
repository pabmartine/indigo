package com.martinia.indigo.tag.application.cover;

import com.martinia.indigo.tag.domain.model.TagCoverResult;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.cover.FindTagCoverByIdUseCase;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(readOnly = true)
@Slf4j
public class FindTagCoverByIdUseCaseImpl implements FindTagCoverByIdUseCase {

	private static final Duration CACHE_TTL = Duration.ofMinutes(30);

	@Resource
	private TagRepository tagRepository;

	private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

	@Override
	public TagCoverResult getCover(String id) {
		if (StringUtils.isBlank(id)) {
			return TagCoverResult.empty();
		}

		CacheEntry cached = cache.get(id);
		Instant now = Instant.now();
		if (cached != null && Duration.between(cached.createdAt(), now).compareTo(CACHE_TTL) < 0) {
			return cached.result();
		}

		// Try finding cover by ID first; if not found, fallback to finding by name
		Optional<TagMongoEntity> entityOpt = tagRepository.findCoverById(id);
		if (entityOpt.isEmpty()) {
			entityOpt = tagRepository.findCoverByName(id);
		}

		TagCoverResult result = entityOpt
				.map(TagMongoEntity::getImage)
				.filter(StringUtils::isNotBlank)
				.map(this::processImage)
				.orElseGet(TagCoverResult::empty);

		if (!result.isEmpty()) {
			cache.put(id, new CacheEntry(result, now));
		}

		return result;
	}

	private TagCoverResult processImage(String image) {
		String trimmed = image.trim();
		if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
			try {
				return TagCoverResult.redirect(URI.create(trimmed));
			} catch (Exception ex) {
				log.warn("Invalid image URI for tag: {}", trimmed, ex);
				return TagCoverResult.empty();
			}
		}

		String base64 = trimmed;
		if (base64.startsWith("data:")) {
			int comma = base64.indexOf(',');
			if (comma != -1) {
				base64 = base64.substring(comma + 1);
			}
		}

		try {
			byte[] bytes = Base64.getDecoder().decode(base64);
			return TagCoverResult.bytes(bytes);
		} catch (IllegalArgumentException ex) {
			log.warn("Invalid Base64 for tag image: {}", ex.getMessage());
			return TagCoverResult.empty();
		}
	}

	private record CacheEntry(TagCoverResult result, Instant createdAt) {
	}
}
