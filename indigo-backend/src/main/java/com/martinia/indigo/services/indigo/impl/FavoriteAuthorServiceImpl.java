package com.martinia.indigo.services.indigo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.indigo.FavoriteAuthor;
import com.martinia.indigo.repository.indigo.FavoriteAuthorRepository;
import com.martinia.indigo.services.indigo.FavoriteAuthorService;

@Service
public class FavoriteAuthorServiceImpl implements FavoriteAuthorService {

	@Autowired
	FavoriteAuthorRepository favoriteAuthorRepository;


	@Override
	public FavoriteAuthor getFavoriteAuthor(int author, int user) {
		return favoriteAuthorRepository.findByAuthorAndUser(author, user);
	}

	@Override
	public void save(FavoriteAuthor fb) {
		favoriteAuthorRepository.save(fb);
	}

	@Override
	public void deleteFavoriteAuthors(int author, int user) {
		FavoriteAuthor fb = this.getFavoriteAuthor(author, user);
		favoriteAuthorRepository.delete(fb);
	}

}
