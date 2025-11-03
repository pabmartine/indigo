package com.martinia.indigo.user.application;

import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.domain.ports.usecases.DeleteUserUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteUserUseCaseImpl implements DeleteUserUseCase {

	@Resource
	private UserRepository userRepository;

	@Resource
	private PasswordEncoder passwordEncoder;

	@Override
	public void delete(final String id) {
		userRepository.findById(id).ifPresent(user -> userRepository.delete(user));
	}
}