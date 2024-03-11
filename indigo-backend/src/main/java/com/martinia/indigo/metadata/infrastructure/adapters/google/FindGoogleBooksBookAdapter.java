package com.martinia.indigo.metadata.infrastructure.adapters.google;

import com.martinia.indigo.metadata.domain.ports.adapters.google.FindGoogleBooksBookPort;
import com.martinia.indigo.metadata.domain.ports.usecases.google.FindGoogleBooksBookUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@ConditionalOnProperty(name = "flags.google", havingValue="true")
public class FindGoogleBooksBookAdapter implements FindGoogleBooksBookPort {

	@Resource
	private FindGoogleBooksBookUseCase findGoogleBooksBookUseCase;

	@Override
	public String[] findBook(final String title, final List<String> authors) {
		return findGoogleBooksBookUseCase.findBook(title, authors);
	}
}
