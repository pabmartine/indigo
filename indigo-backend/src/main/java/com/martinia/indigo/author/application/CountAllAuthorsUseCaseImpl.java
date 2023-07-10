package com.martinia.indigo.author.application;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.CountAllAuthorsUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CountAllAuthorsUseCaseImpl implements CountAllAuthorsUseCase {

	@Resource
	private AuthorRepository authorRepository;

	@Override
	public Long count(List<String> languages) {
		return authorRepository.count(languages);
	}

}
