package com.martinia.indigo.user.domain.ports.usecases;

import com.martinia.indigo.user.domain.model.User;

import java.util.Optional;

public interface FindUserByUsernameUseCase {

	Optional<User> findByUsername(String username);

}
