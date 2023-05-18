package com.martinia.indigo.user.application;

import com.martinia.indigo.ports.out.mongo.UserRepository;
import com.martinia.indigo.user.domain.service.DeleteUserUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DeleteUserUseCaseImpl implements DeleteUserUseCase {

	@Resource
	private UserRepository userRepository;

	@Resource
	private PasswordEncoder passwordEncoder;

	@Override
	public void delete(final String id) {
		userRepository.delete(id);
	}
}