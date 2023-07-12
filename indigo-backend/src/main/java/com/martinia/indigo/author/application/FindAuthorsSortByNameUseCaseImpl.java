package com.martinia.indigo.author.application;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.FindAuthorsSortByNameUseCase;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class FindAuthorsSortByNameUseCaseImpl implements FindAuthorsSortByNameUseCase {

	@Resource
	private AuthorRepository authorRepository;

	@Resource
	private AuthorMongoMapper authorMongoMapper;

	@Override
	public Optional<Author> findBySort(String sort) {
		return authorRepository.findBySort(sort).map(author -> Optional.of(authorMongoMapper.entity2Domain(author)))
				.orElse(Optional.empty());
	}

}
