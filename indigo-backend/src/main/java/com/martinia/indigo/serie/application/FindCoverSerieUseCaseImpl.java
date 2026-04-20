package com.martinia.indigo.serie.application;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.serie.domain.ports.usecases.FindCoverSerieUseCase;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class FindCoverSerieUseCaseImpl implements FindCoverSerieUseCase {

	private static final Duration CACHE_TTL = Duration.ofMinutes(30);

	@Resource
	private BookRepository bookRepository;

	private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

	@Override
	public byte[] getCover(final String serie) {
		String decodedSerie = URLDecoder.decode(serie, StandardCharsets.UTF_8);
		String normalizedSerie = decodedSerie.replace("@_@", "&");

		CacheEntry cached = cache.get(normalizedSerie);
		Instant now = Instant.now();
		if (cached != null && Duration.between(cached.createdAt(), now).compareTo(CACHE_TTL) < 0) {
			return cached.cover();
		}

		byte[] cover = bookRepository.findFirstImageBySerie(normalizedSerie)
				.map(this::decodeImage)
				.filter(bytes -> bytes.length > 0)
				.orElseGet(() -> new byte[0]);

		if (cover.length > 0) {
			cache.put(normalizedSerie, new CacheEntry(cover, now));
		}

		return cover;
	}

	private byte[] decodeImage(String image) {
		if (StringUtils.isBlank(image)) {
			return new byte[0];
		}

		try {
			return Base64.getDecoder().decode(image);
		}
		catch (IllegalArgumentException ex) {
			return image.getBytes(StandardCharsets.UTF_8);
		}
	}

	private record CacheEntry(byte[] cover, Instant createdAt) {
	}
}
