package com.martinia.indigo.user.domain.service;

import com.martinia.indigo.user.domain.model.User;

public interface SaveUserUseCase {

	void save(User user, boolean isNew);

}
