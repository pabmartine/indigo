package com.martinia.indigo.adapters.out.mongo.service;

import com.martinia.indigo.adapters.out.mongo.repository.UserMongoRepository;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.usecases.FindAuthorsSortByNameUseCase;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.mappers.UserMongoMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserRepositoryImpl implements UserRepository {

	@Resource
	private UserMongoRepository userMongoRepository;

	@Resource
	private BookMongoRepository bookMongoRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Resource
	private FindAuthorsSortByNameUseCase findAuthorsSortByNameUseCase;

	@Resource
	private UserMongoMapper userMongoMapper;

	@Override
	public void addFavoriteAuthor(String user, String author) {

		UserMongoEntity _user = userMongoRepository.findByUsername(user).get();
		if (CollectionUtils.isEmpty(_user.getFavoriteAuthors()) || !_user.getFavoriteAuthors().contains(author)) {
			_user.getFavoriteAuthors().add(author);
			userMongoRepository.save(_user);
		}
	}

	@Override
	public void addFavoriteBook(String user, String book) {

		UserMongoEntity _user = userMongoRepository.findByUsername(user).get();
		if (CollectionUtils.isEmpty(_user.getFavoriteBooks()) || !_user.getFavoriteBooks().contains(book)) {
			_user.getFavoriteBooks().add(book);
			userMongoRepository.save(_user);
		}
	}

	@Override
	public void delete(String id) {
		UserMongoEntity user = userMongoRepository.findById(id).get();
		userMongoRepository.delete(user);
	}

	@Override
	public void deleteFavoriteAuthor(String user, String author) {
		UserMongoEntity _user = userMongoRepository.findByUsername(user).get();
		_user.getFavoriteAuthors().remove(author);
		userMongoRepository.save(_user);
	}

	@Override
	public void deleteFavoriteBook(String user, String book) {
		UserMongoEntity _user = userMongoRepository.findByUsername(user).get();
		_user.getFavoriteBooks().remove(book);
		userMongoRepository.save(_user);

	}

	@Override
	public List<User> findAll() {
		List<UserMongoEntity> users = userMongoRepository.findAll();
		return userMongoMapper.entities2Domains(users);
	}

	@Override
	public Optional<User> findById(String id) {
		return userMongoRepository.findById(id).map(user -> Optional.of(userMongoMapper.entity2Domain(user))).orElse(Optional.empty());
	}

	@Override
	public Optional<User> findByUsername(String username) {
		Optional<UserMongoEntity> user = userMongoRepository.findByUsername(username);
		return user.map(_user -> Optional.of(userMongoMapper.entity2Domain(user.get()))).orElse(Optional.empty());
	}

	@Override
	public List<Book> getFavoriteBooks(String user) {

		List<String> books = userMongoRepository.findByUsername(user).get().getFavoriteBooks();
		List<Book> ret = new ArrayList<>(books != null ? books.size() : 0);
		if (books != null) {
			books.forEach(path -> {
				Optional<Book> _book = bookMongoRepository.findByPath(path).map(book -> Optional.of(bookMongoMapper.entity2Domain(book)))
						.orElse(Optional.empty());
				if (_book.isPresent()) {
					ret.add(_book.get());
				}
			});
		}
		return ret;
	}

	@Override
	public List<Author> getFavoriteAuthors(String user) {
		List<String> authors = userMongoRepository.findByUsername(user).get().getFavoriteAuthors();
		List<Author> ret = new ArrayList<>(authors != null ? authors.size() : 0);
		if (!CollectionUtils.isEmpty(authors)) {
			authors.forEach(author -> ret.add(findAuthorsSortByNameUseCase.findBySort(author).get()));
		}
		return ret;
	}

	@Override
	public Boolean isFavoriteAuthor(String user, String author) {
		UserMongoEntity _user = userMongoRepository.findByUsername(user).get();
		return _user.getFavoriteAuthors().stream().filter(favorite -> favorite.equalsIgnoreCase(author)).findAny().isPresent();
	}

	@Override
	public Boolean isFavoriteBook(String user, String book) {
		UserMongoEntity _user = userMongoRepository.findByUsername(user).get();
		return _user.getFavoriteBooks().stream().filter(favorite -> favorite.equalsIgnoreCase(book)).findAny().isPresent();
	}

	@Override
	public void save(User user) {
		userMongoRepository.save(userMongoMapper.domain2Entity(user));
	}

}
