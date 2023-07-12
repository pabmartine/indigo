package com.martinia.indigo.user.application;

import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByIdUseCase;
import com.martinia.indigo.user.infrastructure.mongo.mappers.UserMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class FindUserByIdUseCaseImpl implements FindUserByIdUseCase {

	@Resource
	private UserRepository userRepository;

	@Resource
	private UserMongoMapper userMongoMapper;

	@Override
	public Optional<User> findById(final String id) {
		return userRepository.findById(id).map(user -> Optional.of(userMongoMapper.entity2Domain(user))).orElse(Optional.empty());
	}

}
