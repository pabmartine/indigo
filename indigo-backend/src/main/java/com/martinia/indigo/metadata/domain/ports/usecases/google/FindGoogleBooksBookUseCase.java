package com.martinia.indigo.metadata.domain.ports.usecases.google;

import java.util.List;

public interface FindGoogleBooksBookUseCase {

	String[] findBook(String title, List<String> authors);

}
