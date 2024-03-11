package com.martinia.indigo.metadata.infrastructure.adapters.wikipedia;

import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorInfoPort;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorInfoUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ConditionalOnProperty(name = "flags.wikipedia", havingValue="true")
public class FindWikipediaAuthorInfoAdapter implements FindWikipediaAuthorInfoPort {

	@Resource
	private FindWikipediaAuthorInfoUseCase useCase;

	@Override
	public String[] getAuthorInfo(final String subject, final String lang) {
		return useCase.getAuthorInfo(subject, lang);
	}
}
