package com.martinia.indigo.user.application;

import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByUsernameUseCase;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.mappers.UserMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class FindUserByUsernameUseCaseImpl implements FindUserByUsernameUseCase {

	@Resource
	private UserRepository userRepository;
	@Resource
	private UserMongoMapper userMongoMapper;

	@Override
	public Optional<User> findByUsername(final String username) {
		Optional<UserMongoEntity> user = userRepository.findByUsername(username);
		return user.map(_user -> Optional.of(userMongoMapper.entity2Domain(user.get()))).orElse(Optional.empty());
	}

}
