package com.martinia.indigo.services.indigo.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.indigo.MyBook;
import com.martinia.indigo.repository.indigo.FavoriteBookRepository;
import com.martinia.indigo.repository.indigo.MyBookRepository;
import com.martinia.indigo.repository.indigo.NotificationRepository;
import com.martinia.indigo.services.indigo.MyBookService;

@Service
public class MyBookServiceImpl implements MyBookService {

	@Autowired
	MyBookRepository myBookRepository;
	
	@Autowired
	NotificationRepository notificationRepository;
	
	@Autowired
	FavoriteBookRepository favoriteBookRepository;

	@Override
	public List<Integer> getSentBooks(int user) {
		return notificationRepository.getSentBooks(user);
	}

	@Override
	public void save(MyBook myBook) {
		myBookRepository.save(myBook);
	}

	@Override
	public Optional<MyBook> findById(int id) {
		return myBookRepository.findById(id);
	}

	@Override
	public List<Integer> getFavoriteBooks(int user) {
		return favoriteBookRepository.findBooksByUser(user);
	}

}
