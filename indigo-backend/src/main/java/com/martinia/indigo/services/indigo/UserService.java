package com.martinia.indigo.services.indigo;

import java.util.Optional;

import com.martinia.indigo.model.indigo.User;

public interface UserService {

	User findByUsername(String username);

	Optional<User> findById(int user);

	void save(User user, boolean isNew);

	Iterable<User> findAll();

	void delete(int id);

	void update(User user);

}
