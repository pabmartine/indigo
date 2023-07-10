package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.usecases.favorite.FindFavoriteAuthorsUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FindFavoriteAuthorsUseCaseImpl implements FindFavoriteAuthorsUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public List<Author> getFavoriteAuthors(String user) {
		return userRepository.getFavoriteAuthors(user);

	}

}