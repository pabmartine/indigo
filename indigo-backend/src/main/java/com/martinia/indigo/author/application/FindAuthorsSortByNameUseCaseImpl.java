package com.martinia.indigo.author.application;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.repository.AuthorRepository;
import com.martinia.indigo.author.domain.service.FindAuthorsSortByNameUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class FindAuthorsSortByNameUseCaseImpl implements FindAuthorsSortByNameUseCase {

	@Resource
	private AuthorRepository authorRepository;

	@Override
	public Optional<Author> findBySort(String name) {
		return authorRepository.findBySort(name);
	}

}
