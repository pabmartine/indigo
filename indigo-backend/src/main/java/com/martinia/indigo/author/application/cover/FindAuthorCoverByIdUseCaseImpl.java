package com.martinia.indigo.author.application.cover;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.cover.FindAuthorCoverByIdUseCase;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
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
public class FindAuthorCoverByIdUseCaseImpl implements FindAuthorCoverByIdUseCase {

	@Resource
	private AuthorRepository authorRepository;

	@Override
	public Optional<byte[]> getCover(String authorId) {
		if (StringUtils.isBlank(authorId)) {
			return Optional.empty();
		}

		return authorRepository.findCoverById(authorId)
				.map(AuthorMongoEntity::getImage)
				.filter(StringUtils::isNotBlank)
				.flatMap(this::decodeImage)
				.or(() -> {
					log.warn("Cover not found for author {}", authorId);
					return Optional.empty();
				});
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
