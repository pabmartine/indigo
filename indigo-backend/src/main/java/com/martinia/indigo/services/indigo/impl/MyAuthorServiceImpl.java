package com.martinia.indigo.services.indigo.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.indigo.MyAuthor;
import com.martinia.indigo.repository.indigo.MyAuthorRepository;
import com.martinia.indigo.services.indigo.MyAuthorService;

@Service
public class MyAuthorServiceImpl implements MyAuthorService {

	@Autowired
	MyAuthorRepository myAuthorRepository;

	@Override
	public List<Integer> getFavoriteAuthors(int user) {
		return myAuthorRepository.getFavoriteAuthors(user);
	}

	@Override
	public Optional<MyAuthor> findById(int id) {
		return myAuthorRepository.findById(id);
	}

	@Override
	public void save(MyAuthor myAuthor) {
		myAuthorRepository.save(myAuthor);
	}

	@Override
	public MyAuthor findBySort(String author) {
		return myAuthorRepository.findBySort(author);
	}

}
