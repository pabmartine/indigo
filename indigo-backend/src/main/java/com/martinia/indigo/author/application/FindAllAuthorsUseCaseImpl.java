package com.martinia.indigo.author.application;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.repository.AuthorRepository;
import com.martinia.indigo.author.domain.service.FindAllAuthorsUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FindAllAuthorsUseCaseImpl implements FindAllAuthorsUseCase {

	@Resource
	private AuthorRepository authorRepository;

	@Override
	public List<Author> findAll(List<String> languages, int page, int size, String sort, String order) {
		return authorRepository.findAll(languages, page, size, sort, order);
	}

}
