package com.martinia.indigo.user.application;

import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.domain.ports.usecases.FindAllUsersUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FindAllUsersUseCaseImpl implements FindAllUsersUseCase {


	@Resource
	private UserRepository userRepository;

	@Override
	public List<User> findAll() {
		return userRepository.findAll();
	}

}
