package com.martinia.indigo.services.indigo.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.indigo.User;
import com.martinia.indigo.repository.indigo.UserRepository;
import com.martinia.indigo.services.indigo.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Override
	public User findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public Optional<User> findById(int user) {
		return userRepository.findById(user);
	}

	@Override
	public void delete(User user) {
		userRepository.delete(user);
	}

	@Override
	public void save(User user) {
		userRepository.save(user);
	}

	@Override
	public Iterable<User> findAll() {
		return userRepository.findAll();
	}

}
