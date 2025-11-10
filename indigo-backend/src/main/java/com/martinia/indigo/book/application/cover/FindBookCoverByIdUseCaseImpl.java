package com.martinia.indigo.book.application.cover;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.cover.FindBookCoverByIdUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class FindBookCoverByIdUseCaseImpl implements FindBookCoverByIdUseCase {

	@Resource
	private BookRepository bookRepository;

	@Override
	public Optional<byte[]> getCover(String bookId) {
		if (StringUtils.isBlank(bookId)) {
			return Optional.empty();
		}

		return bookRepository.findById(bookId)
				.map(BookMongoEntity::getImage)
				.filter(StringUtils::isNotBlank)
				.map(this::decodeImage)
				.or(() -> {
					log.warn("Cover not found for book {}", bookId);
					return Optional.empty();
				});
	}

	private byte[] decodeImage(String image) {
		try {
			return Base64.getDecoder().decode(image);
		}
		catch (IllegalArgumentException ex) {
			log.error("Failed to decode cover image", ex);
			return null;
		}
	}
}
