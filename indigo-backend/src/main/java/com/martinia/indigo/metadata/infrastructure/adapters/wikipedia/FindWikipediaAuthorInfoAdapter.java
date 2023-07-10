package com.martinia.indigo.metadata.infrastructure.adapters.wikipedia;

import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorInfoPort;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorInfoUseCase;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FindWikipediaAuthorInfoAdapter implements FindWikipediaAuthorInfoPort {

	@Resource
	private FindWikipediaAuthorInfoUseCase useCase;

	@Override
	public String[] getAuthorInfo(final String subject, final String lang) {
		return useCase.getAuthorInfo(subject, lang);
	}
}
