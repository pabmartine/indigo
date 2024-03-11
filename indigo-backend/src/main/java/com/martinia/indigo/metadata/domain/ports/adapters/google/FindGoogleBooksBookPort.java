package com.martinia.indigo.metadata.domain.ports.adapters.google;

import java.util.List;

public interface FindGoogleBooksBookPort {

	String[] findBook(String title, List<String> authors);

}
