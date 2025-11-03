package com.martinia.indigo.author.domain.ports.usecases;

import java.util.List;

public interface CountAllAuthorsUseCase {

	Long count(List<String> languages);
	Long countAllAuthors(); // Add this method

}