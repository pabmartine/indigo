package com.martinia.indigo.metadata.domain.ports.adapters.goodreads;

import java.util.List;

public interface FindGoodReadsBookPort {

	String[] findBook(String key, String title, List<String> authors, boolean withAuthor);

}
