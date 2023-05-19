package com.martinia.indigo.adapters.out.mongo.service;

import com.martinia.indigo.adapters.out.mongo.entities.UserMongoEntity;
import com.martinia.indigo.adapters.out.mongo.mapper.UserMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.UserMongoRepository;
import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.User;
import com.martinia.indigo.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        UserMongoEntity _user = userMongoRepository.findByUsername(user).get();
        if (CollectionUtils.isEmpty(_user.getFavoriteAuthors()) || !_user.getFavoriteAuthors()
                .contains(author)) {
            _user.getFavoriteAuthors()
                    .add(author);
            userMongoRepository.save(_user);
        }
    }

    @Override
    public void addFavoriteBook(String user, String book) {

        UserMongoEntity _user = userMongoRepository.findByUsername(user).get();
        if (CollectionUtils.isEmpty(_user.getFavoriteBooks()) || !_user.getFavoriteBooks()
                .contains(book)) {
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
        UserMongoEntity _user = userMongoRepository.findByUsername(user).get();
        _user.getFavoriteAuthors()
                .remove(author);
        userMongoRepository.save(_user);
    }

    @Override
    public void deleteFavoriteBook(String user, String book) {
        UserMongoEntity _user = userMongoRepository.findByUsername(user).get();
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

        List<String> books = userMongoRepository.findByUsername(user).get()
                .getFavoriteBooks();
        List<Book> ret = new ArrayList<>(books != null ? books.size() : 0);
        if (books != null)
            books.forEach(book -> {
                Optional<Book> _book = bookMongoRepository.findByPath(book);
                if (_book.isPresent())
                    ret.add(_book.get());
            });
        return ret;
    }

    @Override
    public List<Author> getFavoriteAuthors(String user) {
        List<String> authors = userMongoRepository.findByUsername(user).get()
                .getFavoriteAuthors();
        List<Author> ret = new ArrayList<>(authors != null ? authors.size() : 0);
        if (!CollectionUtils.isEmpty(authors))
            authors.forEach(author -> ret.add(authorMongoRepository.findBySort(author).get()));
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
