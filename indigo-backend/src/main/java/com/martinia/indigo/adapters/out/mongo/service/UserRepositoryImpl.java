package com.martinia.indigo.adapters.out.mongo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.martinia.indigo.adapters.out.mongo.entities.UserMongoEntity;
import com.martinia.indigo.adapters.out.mongo.mapper.UserMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.UserMongoRepository;
import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.User;
import com.martinia.indigo.ports.out.mongo.UserRepository;

@Component
public class UserRepositoryImpl implements UserRepository {

	@Autowired
	UserMongoRepository userMongoRepository;

	@Autowired
	BookRepositoryImpl bookMongoRepository;

	@Autowired
	AuthorRepositoryImpl authorMongoRepository;

	@Autowired
	UserMongoMapper userMongoMapper;

	@Override
	public void addFavoriteAuthor(String user, String author) {

		UserMongoEntity _user = userMongoRepository.findById(user)
				.get();
		if (CollectionUtils.isEmpty(_user.getFavoriteAuthors()) || !_user.getFavoriteAuthors()
				.contains(author)) {
			if (_user.getFavoriteAuthors() == null)
				_user.setFavoriteAuthors(new ArrayList<>());
			_user.getFavoriteAuthors()
					.add(author);
			userMongoRepository.save(_user);
		}
	}

	@Override
	public void addFavoriteBook(String user, String book) {

		UserMongoEntity _user = userMongoRepository.findById(user)
				.get();
		if (CollectionUtils.isEmpty(_user.getFavoriteBooks()) || !_user.getFavoriteBooks()
				.contains(book)) {
			if (_user.getFavoriteBooks() == null)
				_user.setFavoriteBooks(new ArrayList<>());
			_user.getFavoriteBooks()
					.add(book);
			userMongoRepository.save(_user);
		}
	}

	@Override
	public void delete(String id) {
		UserMongoEntity user = userMongoRepository.findById(id)
				.get();
		userMongoRepository.delete(user);
	}

	@Override
	public void deleteFavoriteAuthor(String user, String author) {
		UserMongoEntity _user = userMongoRepository.findById(user)
				.get();
		_user.getFavoriteAuthors()
				.remove(author);
		userMongoRepository.save(_user);
	}

	@Override
	public void deleteFavoriteBook(String user, String book) {
		UserMongoEntity _user = userMongoRepository.findById(user)
				.get();
		_user.getFavoriteBooks()
				.remove(book);
		userMongoRepository.save(_user);

	}

	@Override
	public List<User> findAll() {
		List<UserMongoEntity> users = userMongoRepository.findAll();
		return userMongoMapper.entities2Domains(users);
	}

	@Override
	public User findById(String id) {
		UserMongoEntity user = userMongoRepository.findById(id)
				.get();
		return userMongoMapper.entity2Domain(user);
	}

	@Override
	public User findByUsername(String username) {
		UserMongoEntity user = userMongoRepository.findByUsername(username);
		return userMongoMapper.entity2Domain(user);
	}

	@Override
	public List<Book> getFavoriteBooks(String user) {

		List<String> books = userMongoRepository.findById(user)
				.get()
				.getFavoriteBooks();
		List<Book> ret = new ArrayList<Book>(books.size());
		for (String book : books) {
			Book _book = bookMongoRepository.findByPath(book);
			if (_book != null)
				ret.add(_book);
		}
		return ret;
	}

	@Override
	public List<Author> getFavoriteAuthors(String user) {
		List<String> authors = userMongoRepository.findById(user)
				.get()
				.getFavoriteAuthors();
		List<Author> ret = new ArrayList<Author>(authors.size());
		for (String author : authors) {
			ret.add(authorMongoRepository.findBySort(author));
		}
		return ret;
	}

	@Override
	public Boolean isFavoriteAuthor(String user, String author) {
		boolean ret = false;
		UserMongoEntity _user = userMongoRepository.findById(user)
				.get();
		if (!CollectionUtils.isEmpty(_user.getFavoriteAuthors()) && _user.getFavoriteAuthors()
				.contains(author)) {
			ret = true;
		}
		return ret;
	}

	@Override
	public Boolean isFavoriteBook(String user, String book) {
		boolean ret = false;
		UserMongoEntity _user = userMongoRepository.findById(user)
				.get();
		if (!CollectionUtils.isEmpty(_user.getFavoriteBooks()) && _user.getFavoriteBooks()
				.contains(book)) {
			ret = true;
		}
		return ret;
	}

	@Override
	public void save(User user) {
		userMongoRepository.save(userMongoMapper.domain2Entity(user));
	}

}
