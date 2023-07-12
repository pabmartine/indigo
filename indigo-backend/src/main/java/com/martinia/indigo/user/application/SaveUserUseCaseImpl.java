package com.martinia.indigo.user.application;

import com.martinia.indigo.user.domain.model.RolesEnum;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.domain.ports.usecases.SaveUserUseCase;
import com.martinia.indigo.user.infrastructure.mongo.mappers.UserMongoMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SaveUserUseCaseImpl implements SaveUserUseCase {

	@Resource
	private UserRepository userRepository;

	@Resource
	private UserMongoMapper userMongoMapper;

	@Resource
	private PasswordEncoder passwordEncoder;

	@Override
	public void save(final User user, final boolean isNew) {
		if (isNew) {
			user.setRole(RolesEnum.USER.name());
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		userRepository.save(userMongoMapper.domain2Entity(user));
	}
}