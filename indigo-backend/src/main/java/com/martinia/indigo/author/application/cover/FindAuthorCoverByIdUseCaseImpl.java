package com.martinia.indigo.author.application.cover;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.cover.FindAuthorCoverByIdUseCase;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@Slf4j
public class FindAuthorCoverByIdUseCaseImpl implements FindAuthorCoverByIdUseCase {

	private static final Duration CACHE_TTL = Duration.ofMinutes(30);

	@Resource
	private AuthorRepository authorRepository;

	private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

	@Override
	public Optional<byte[]> getCover(String authorId) {
		if (StringUtils.isBlank(authorId)) {
			return Optional.empty();
		}

		CacheEntry cached = cache.get(authorId);
		Instant now = Instant.now();
		if (cached != null && Duration.between(cached.createdAt(), now).compareTo(CACHE_TTL) < 0) {
			return cached.cover();
		}

		Optional<byte[]> result = authorRepository.findCoverById(authorId)
				.map(AuthorMongoEntity::getImage)
				.filter(StringUtils::isNotBlank)
				.flatMap(this::decodeImage);

		cache.put(authorId, new CacheEntry(result, now));
		return result;
	}

	private record CacheEntry(Optional<byte[]> cover, Instant createdAt) {
	}

	private Optional<byte[]> decodeImage(String image) {
		try {
			if (image.startsWith("data:")) {
				int commaIndex = image.indexOf(",");
				if (commaIndex != -1) {
					image = image.substring(commaIndex + 1);
				}
			}
			return Optional.of(Base64.getDecoder().decode(image));
		}
		catch (IllegalArgumentException ex) {
			log.error("Failed to decode author cover image", ex);
			return Optional.empty();
		}
	}

}
