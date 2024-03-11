package com.martinia.indigo.user.domain.ports.usecases;

import com.martinia.indigo.user.domain.model.User;

import java.util.List;

public interface FindAllUsersUseCase {

	List<User> findAll();

}
