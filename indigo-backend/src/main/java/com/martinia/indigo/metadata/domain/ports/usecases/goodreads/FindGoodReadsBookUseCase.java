package com.martinia.indigo.metadata.domain.ports.usecases.goodreads;

import java.util.List;

public interface FindGoodReadsBookUseCase {

	String[] findBook(String key, String title, List<String> authors, boolean withAuthor);

}
