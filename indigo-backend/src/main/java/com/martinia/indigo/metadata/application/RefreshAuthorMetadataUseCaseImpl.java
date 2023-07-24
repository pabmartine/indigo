package com.martinia.indigo.metadata.application;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.domain.ports.commands.FindAuthorMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.RefreshAuthorMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class RefreshAuthorMetadataUseCaseImpl implements RefreshAuthorMetadataUseCase {

	@Resource
	private AuthorRepository authorRepository;
	@Resource
	private AuthorMongoMapper mapper;
	@Resource
	private CommandBus commandBus;

	@Override
	public Optional<Author> findAuthorMetadata(String sort, String lang) {

		return authorRepository.findBySort(sort).map(author -> {
			commandBus.executeAndWait(
					FindAuthorMetadataCommand.builder().authorId(author.getId()).override(true).lastExecution(0).lang(lang).build());
			return Optional.of(mapper.entity2Domain(authorRepository.findBySort(sort).get()));
		}).orElse(Optional.empty());

	}

}
