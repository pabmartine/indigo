package com.martinia.indigo.metadata.application;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.metadata.application.common.BaseMetadataUseCaseImpl;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshAuthorMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class RefreshAuthorMetadataUseCaseImpl extends BaseMetadataUseCaseImpl implements RefreshAuthorMetadataUseCase {

	@Override
	public Optional<Author> findAuthorMetadata(String sort, String lang) {

		return authorRepository.findBySort(sort).map(author -> {
			AuthorMongoEntity _author = findAuthorMetadata(lang, true, author);
			authorRepository.save(_author);
			return Optional.of(authorMongoMapper.entity2Domain(_author));
		}).orElse(Optional.empty());

	}

}
