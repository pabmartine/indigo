package com.martinia.indigo.user.application;

import com.martinia.indigo.domain.model.User;
import com.martinia.indigo.user.domain.repository.UserRepository;
import com.martinia.indigo.user.domain.service.FindUserByUsernameUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FindUserByUsernameUseCaseImpl implements FindUserByUsernameUseCase {

	@Autowired
	UserRepository userRepository;

	@Override
	public Optional<User> findByUsername(final String username) {
		return userRepository.findByUsername(username);
	}

}
