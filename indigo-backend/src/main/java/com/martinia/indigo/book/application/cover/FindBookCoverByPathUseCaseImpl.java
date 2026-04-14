package com.martinia.indigo.book.application.cover;

import com.martinia.indigo.book.domain.ports.usecases.cover.FindBookCoverByPathUseCase;
import com.martinia.indigo.common.util.ImageUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class FindBookCoverByPathUseCaseImpl implements FindBookCoverByPathUseCase {

	private static final Duration CACHE_TTL = Duration.ofMinutes(10);

	@Resource
	private ImageUtils imageUtils;

	private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

	@Override
	public Optional<String> getImage(String path) {
		if (StringUtils.isBlank(path)) {
			return Optional.empty();
		}

		CacheEntry cached = cache.get(path);
		Instant now = Instant.now();
		if (cached != null && Duration.between(cached.createdAt(), now).compareTo(CACHE_TTL) < 0) {
			return Optional.of(cached.image());
		}

		return Optional.ofNullable(imageUtils.getBase64Cover(path, true))
				.map(image -> {
					cache.put(path, new CacheEntry(image, now));
					return image;
				});
	}

	private record CacheEntry(String image, Instant createdAt) {
	}
}
