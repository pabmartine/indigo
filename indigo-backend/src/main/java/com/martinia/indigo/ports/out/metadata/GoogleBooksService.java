package com.martinia.indigo.ports.out.metadata;

import java.util.List;

public interface GoogleBooksService {

	String[] findBook(String title, List<String> authors);

}
