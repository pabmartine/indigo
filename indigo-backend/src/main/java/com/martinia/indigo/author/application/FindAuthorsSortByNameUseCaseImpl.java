package com.martinia.indigo.author.application;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.FindAuthorsSortByNameUseCase;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional
public class FindAuthorsSortByNameUseCaseImpl implements FindAuthorsSortByNameUseCase {

	@Resource
	private AuthorRepository authorRepository;

	@Resource
	private AuthorMongoMapper authorMongoMapper;

	@Override
	public Optional<Author> findBySort(String sort) {
		Optional<AuthorMongoEntity> authorEntity = authorRepository.findBySort(sort);
		if (authorEntity.isEmpty()) {
			authorEntity = authorRepository.findByName(sort);
		}
		return authorEntity.map(authorMongoMapper::entity2Domain);
	}

}
