package com.martinia.indigo.services.indigo.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.martinia.indigo.enums.RolesEnum;
import com.martinia.indigo.model.indigo.User;
import com.martinia.indigo.repository.indigo.UserRepository;
import com.martinia.indigo.services.indigo.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;
	

	@Autowired
	PasswordEncoder passwordEncoder;

	@Override
	public User findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public Optional<User> findById(int user) {
		return userRepository.findById(user);
	}

	@Override
	public void save(User user, boolean isNew) {
		if (isNew) {
			user.setRole(RolesEnum.USER.name());
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		userRepository.save(user);
	}

	@Override
	public List<User> findAll() {
		return userRepository.findAll();
	}

	@Override
	public void delete(int id) {
		User user = this.findById(id)
				.get();
		userRepository.delete(user);
	}

	@Override
	public void update(User user) {
		User _user = this.findById(user.getId())
				.get();

		if (!_user.getPassword()
				.equals(user.getPassword()))
			_user.setPassword(passwordEncoder.encode(user.getPassword()));

		_user.setUsername(user.getUsername());
		_user.setLanguage(user.getLanguage());
		_user.setKindle(user.getKindle());

		this.save(_user, false);		
	}

}
