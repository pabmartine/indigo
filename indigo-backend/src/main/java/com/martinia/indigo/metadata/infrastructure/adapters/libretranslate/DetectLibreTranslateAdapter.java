package com.martinia.indigo.metadata.infrastructure.adapters.libretranslate;

import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.DetectLibreTranslatePort;
import com.martinia.indigo.metadata.domain.ports.usecases.libretranslate.DetectLibreTranslateUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ConditionalOnProperty(name = "flags.libretranslate", havingValue="true")
public class DetectLibreTranslateAdapter implements DetectLibreTranslatePort {

	@Resource
	private DetectLibreTranslateUseCase useCase;

	@Override
	public String detect(final String text) {
		return useCase.detect(text);
	}
}
