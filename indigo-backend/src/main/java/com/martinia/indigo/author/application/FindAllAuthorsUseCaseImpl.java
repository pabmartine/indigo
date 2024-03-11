package com.martinia.indigo.author.application;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.FindAllAuthorsUseCase;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FindAllAuthorsUseCaseImpl implements FindAllAuthorsUseCase {

	@Resource
	private AuthorRepository authorRepository;

	@Resource
	private AuthorMongoMapper authorMongoMapper;

	@Value("${data.author.default-image}")
	private String defaultImage;

	@Override
	public List<Author> findAll(List<String> languages, int page, int size, String sort, String order) {

		List<AuthorMongoEntity> authors = authorRepository.findAll(languages,
				PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sort)));

		authors = authors.stream().map(author -> {
			author.getNumBooks().setTotal(author.getNumBooks().getLanguages().keySet().stream().filter(lang -> languages.contains(lang))
					.mapToInt(lang -> author.getNumBooks().getLanguages().get(lang)).sum());
			if (Optional.ofNullable(author.getImage()).isPresent() && author.getImage().equals(defaultImage)) {
				author.setImage(null);
			}
			return author;
		}).collect(Collectors.toList());

		return authorMongoMapper.entities2Domains(authors);
	}

}
