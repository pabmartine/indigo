package com.martinia.indigo.user.domain.service;

import com.martinia.indigo.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface FindAllUsersUseCase {

	List<User> findAll();

}
