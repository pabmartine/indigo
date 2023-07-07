package com.martinia.indigo.user.application;

import com.martinia.indigo.user.domain.model.RolesEnum;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.repository.UserRepository;
import com.martinia.indigo.user.domain.service.SaveUserUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SaveUserUseCaseImpl implements SaveUserUseCase {

	@Resource
	private UserRepository userRepository;

	@Resource
	private PasswordEncoder passwordEncoder;

	@Override
	public void save(final User user, final boolean isNew) {
		if (isNew) {
			user.setRole(RolesEnum.USER.name());
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		userRepository.save(user);
	}
}