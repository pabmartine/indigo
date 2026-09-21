package com.martinia.indigo.author.application;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.model.AuthorPageData;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.FindAllAuthorsUseCase;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import com.martinia.indigo.common.util.LanguageCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class FindAllAuthorsUseCaseImpl implements FindAllAuthorsUseCase {

	@Resource
	private AuthorRepository authorRepository;

	@Resource
	private AuthorMongoMapper authorMongoMapper;

	@Value("${data.author.default-image}")
	private String defaultImage;

	@Override
	public List<Author> findAll(List<String> languages, int page, int size, String sort, String order) {
		Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
		String sortField = resolveSortField(sort);
		List<AuthorMongoEntity> authors = authorRepository.findAll(languages,
				PageRequest.of(page, size, Sort.by(direction, sortField)));

		java.util.Set<String> requestedVariants = getRequestedVariants(languages);

		authors = authors.stream().map(author -> {
			if (!requestedVariants.isEmpty() && author.getNumBooks() != null && author.getNumBooks().getLanguages() != null) {
				author.getNumBooks().setTotal(author.getNumBooks().getLanguages().entrySet().stream()
						.filter(entry -> requestedVariants.contains(entry.getKey()))
						.mapToInt(java.util.Map.Entry::getValue).sum());
			}
			if (Optional.ofNullable(author.getImage()).isPresent() && author.getImage().equals(defaultImage)) {
				author.setImage(null);
			}
			return author;
		}).collect(Collectors.toList());

		return authorMongoMapper.entities2Domains(authors);
	}

	@Override
	public AuthorPageData findSummaryPage(List<String> languages, int page, int size, String sort, String order) {
		Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
		String sortField = resolveSortField(sort);
		AuthorPageData pageData = authorRepository.findSummaryPage(languages,
				PageRequest.of(page, size, Sort.by(direction, sortField)));

		java.util.Set<String> requestedVariants = getRequestedVariants(languages);

		List<Author> authors = pageData.items().stream().map(author -> {
			if (!requestedVariants.isEmpty() && author.getNumBooks() != null && author.getNumBooks().getLanguages() != null) {
				author.getNumBooks().setTotal(author.getNumBooks().getLanguages().entrySet().stream()
						.filter(entry -> requestedVariants.contains(entry.getKey()))
						.mapToInt(java.util.Map.Entry::getValue).sum());
			}
			return author;
		}).collect(Collectors.toList());

		return new AuthorPageData(authors, pageData.total());
	}

	private java.util.Set<String> getRequestedVariants(List<String> languages) {
		if (languages == null || languages.isEmpty()) {
			return java.util.Collections.emptySet();
		}
		return languages.stream()
				.flatMap(requested -> LanguageCodeUtils.variants(requested).stream())
				.collect(Collectors.toSet());
	}

	private String resolveSortField(String sort) {
		if (sort == null || sort.isBlank() || "name".equalsIgnoreCase(sort)) {
			return "name";
		}
		if ("numBooks".equalsIgnoreCase(sort) || "numBooks.total".equalsIgnoreCase(sort)) {
			return "numBooks.total";
		}
		if ("id".equalsIgnoreCase(sort)) {
			return "_id";
		}
		return sort;
	}

}
