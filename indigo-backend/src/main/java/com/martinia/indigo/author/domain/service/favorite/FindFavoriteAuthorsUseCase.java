package com.martinia.indigo.author.domain.service.favorite;

import com.martinia.indigo.author.domain.model.Author;

import java.util.List;

public interface FindFavoriteAuthorsUseCase {

	List<Author> getFavoriteAuthors(String user);

}
