package com.martinia.indigo.metadata.application;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.metadata.application.common.BaseMetadataUseCaseImpl;
import com.martinia.indigo.metadata.domain.service.RefreshAuthorMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class RefreshAuthorMetadataUseCaseImpl extends BaseMetadataUseCaseImpl implements RefreshAuthorMetadataUseCase {

	@Override
	public Optional<Author> findAuthorMetadata(String sort, String lang) {

		return authorRepository.findBySort(sort).map(author -> {
			Author _author = findAuthorMetadata(lang, true, author);
			authorRepository.update(_author);
			return Optional.of(_author);
		}).orElse(Optional.empty());

	}

}
