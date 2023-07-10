package com.martinia.indigo.metadata.infrastructure.adapters.libretranslate;

import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.TranslateLibreTranslatePort;
import com.martinia.indigo.metadata.domain.ports.usecases.libretranslate.TranslateLibreTranslateUseCase;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TranslateLibreTranslateAdapter implements TranslateLibreTranslatePort {

	@Resource
	private TranslateLibreTranslateUseCase useCase;

	@Override
	public String translate(final String text, final String target) {
		return useCase.translate(text, target);
	}
}
