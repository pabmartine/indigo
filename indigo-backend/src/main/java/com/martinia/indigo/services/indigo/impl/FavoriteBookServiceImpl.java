package com.martinia.indigo.services.indigo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.indigo.FavoriteBook;
import com.martinia.indigo.repository.indigo.FavoriteBookRepository;
import com.martinia.indigo.services.indigo.FavoriteBookService;

@Service
public class FavoriteBookServiceImpl implements FavoriteBookService {

	@Autowired
	FavoriteBookRepository favoriteBookRepository;

	@Override
	public FavoriteBook getFavoriteBook(int book, int user) {
		return favoriteBookRepository.findByBookAndUser(book, user);
	}

	@Override
	public void delete(FavoriteBook fb) {
		favoriteBookRepository.delete(fb);
	}

	@Override
	public void save(FavoriteBook fb) {
		favoriteBookRepository.save(fb);
	}

}
