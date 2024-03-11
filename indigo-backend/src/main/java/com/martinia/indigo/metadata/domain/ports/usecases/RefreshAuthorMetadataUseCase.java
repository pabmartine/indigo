package com.martinia.indigo.metadata.domain.ports.usecases;

import com.martinia.indigo.author.domain.model.Author;

import java.util.Optional;

public interface RefreshAuthorMetadataUseCase {

	Optional<Author> findAuthorMetadata(String sort, String lang);

}
