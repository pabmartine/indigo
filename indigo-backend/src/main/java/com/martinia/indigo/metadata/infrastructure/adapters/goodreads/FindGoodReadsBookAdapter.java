package com.martinia.indigo.metadata.infrastructure.adapters.goodreads;

import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsBookPort;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsBookUseCase;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class FindGoodReadsBookAdapter implements FindGoodReadsBookPort {

	@Resource
	private FindGoodReadsBookUseCase useCase;

	@Override
	public String[] findBook(final String key, final String title, final List<String> authors, final boolean withAuthor) {
		return useCase.findBook(key, title, authors, withAuthor);
	}
}
