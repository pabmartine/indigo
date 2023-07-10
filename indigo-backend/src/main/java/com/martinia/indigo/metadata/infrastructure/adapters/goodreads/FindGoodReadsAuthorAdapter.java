package com.martinia.indigo.metadata.infrastructure.adapters.goodreads;

import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsAuthorPort;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsAuthorUseCase;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FindGoodReadsAuthorAdapter implements FindGoodReadsAuthorPort {

	@Resource
	private FindGoodReadsAuthorUseCase useCase;

	@Override
	public String[] findAuthor(final String key, final String subject) {
		return useCase.findAuthor(key, subject);
	}
}
