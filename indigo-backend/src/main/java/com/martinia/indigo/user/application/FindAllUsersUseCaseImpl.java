package com.martinia.indigo.user.application;

import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.domain.ports.usecases.FindAllUsersUseCase;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.mappers.UserMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindAllUsersUseCaseImpl implements FindAllUsersUseCase {

	@Resource
	private UserRepository userRepository;

	@Resource
	private UserMongoMapper userMongoMapper;

	@Override
	public List<User> findAll() {
		List<UserMongoEntity> users = userRepository.findAll();
		return userMongoMapper.entities2Domains(users);
	}

}
