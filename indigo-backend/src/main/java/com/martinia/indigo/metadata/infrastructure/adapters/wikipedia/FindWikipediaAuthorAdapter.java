package com.martinia.indigo.metadata.infrastructure.adapters.wikipedia;

import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorPort;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ConditionalOnProperty(name = "flags.wikipedia", havingValue="true")
public class FindWikipediaAuthorAdapter implements FindWikipediaAuthorPort {

	@Resource
	private FindWikipediaAuthorUseCase useCase;

	@Override
	public String[] findAuthor(final String subject, final String lang, final int cont) {
		return useCase.findAuthor(subject, lang, cont);
	}
}
