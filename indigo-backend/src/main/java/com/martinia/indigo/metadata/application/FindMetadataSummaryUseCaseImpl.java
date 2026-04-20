package com.martinia.indigo.metadata.application;

import com.martinia.indigo.author.domain.ports.usecases.CountAllAuthorsUseCase;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.metadata.domain.ports.usecases.FindMetadataSummaryUseCase;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class FindMetadataSummaryUseCaseImpl implements FindMetadataSummaryUseCase {

	@Resource
	private BookRepository bookRepository;

	@Resource
	private CountAllAuthorsUseCase countAllAuthorsUseCase;

	@Override
	public Map<String, Long> getSummary() {
		Map<String, Long> summary = new LinkedHashMap<>();
		summary.put("books", bookRepository.count());
		summary.put("authors", countAllAuthorsUseCase.countAllAuthors());
		summary.put("reviews", bookRepository.countReviews());
		return summary;
	}
}
