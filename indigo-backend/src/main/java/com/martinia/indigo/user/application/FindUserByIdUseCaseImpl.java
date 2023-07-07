package com.martinia.indigo.user.application;

import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.repository.UserRepository;
import com.martinia.indigo.user.domain.service.FindUserByIdUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class FindUserByIdUseCaseImpl implements FindUserByIdUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public Optional<User> findById(final String id) {
		return userRepository.findById(id);
	}

}
