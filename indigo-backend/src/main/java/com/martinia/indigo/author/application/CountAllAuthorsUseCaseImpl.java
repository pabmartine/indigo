package com.martinia.indigo.author.application;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.CountAllAuthorsUseCase;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CountAllAuthorsUseCaseImpl implements CountAllAuthorsUseCase {

	@Resource
	private AuthorRepository authorRepository;

	@Override
	public Long count(List<String> languages) {
		return authorRepository.count(languages);
	}

}
