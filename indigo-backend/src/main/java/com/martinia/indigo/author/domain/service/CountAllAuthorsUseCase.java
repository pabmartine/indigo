package com.martinia.indigo.author.domain.service;

import java.util.List;

public interface CountAllAuthorsUseCase {

	Long count(List<String> languages);

}
