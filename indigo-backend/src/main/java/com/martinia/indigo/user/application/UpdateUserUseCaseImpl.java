package com.martinia.indigo.user.application;

import com.martinia.indigo.domain.model.User;
import com.martinia.indigo.ports.out.mongo.UserRepository;
import com.martinia.indigo.user.domain.service.FindUserByIdUseCase;
import com.martinia.indigo.user.domain.service.UpdateUserUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UpdateUserUseCaseImpl implements UpdateUserUseCase {

	@Resource
	private UserRepository userRepository;

	@Resource
	private FindUserByIdUseCase findUserByIdUseCase;

	@Resource
	private PasswordEncoder passwordEncoder;

	@Override
	public void update(final User user) {
		findUserByIdUseCase.findById(user.getId()).ifPresent(_user -> {

			if (!_user.getPassword().equals(user.getPassword())) {
				_user.setPassword(passwordEncoder.encode(user.getPassword()));
			}

			_user.setUsername(user.getUsername());
			_user.setLanguage(user.getLanguage());
			_user.setLanguageBooks(user.getLanguageBooks());
			_user.setKindle(user.getKindle());

			userRepository.save(_user);
		});

	}
}