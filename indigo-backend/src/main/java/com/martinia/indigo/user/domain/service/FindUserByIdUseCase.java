package com.martinia.indigo.user.domain.service;

import com.martinia.indigo.user.domain.model.User;

import java.util.Optional;

public interface FindUserByIdUseCase {

	Optional<User> findById(String id);

}
