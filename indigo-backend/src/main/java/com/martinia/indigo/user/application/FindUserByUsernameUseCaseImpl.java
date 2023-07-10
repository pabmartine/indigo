package com.martinia.indigo.user.application;

import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByUsernameUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class FindUserByUsernameUseCaseImpl implements FindUserByUsernameUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public Optional<User> findByUsername(final String username) {
		return userRepository.findByUsername(username);
	}

}
