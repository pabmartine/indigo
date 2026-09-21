package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.favorite.FindFavoriteAuthorsUseCase;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class FindFavoriteAuthorsUseCaseImpl implements FindFavoriteAuthorsUseCase {

	@Resource
	private UserRepository userRepository;

	@Resource
	private AuthorRepository authorRepository;

	@Resource
	private AuthorMongoMapper authorMongoMapper;

	@Override
	public List<Author> getFavoriteAuthors(String user) {
		return userRepository.findByUsername(user).map(userEntity -> {
			List<String> authors = userEntity.getFavoriteAuthors();
			if (CollectionUtils.isEmpty(authors)) {
				return Collections.<Author>emptyList();
			}

			List<AuthorMongoEntity> entities = authorRepository.findByNameInWithoutImage(authors);
			Map<String, AuthorMongoEntity> entityMap = entities.stream()
					.collect(Collectors.toMap(AuthorMongoEntity::getName, e -> e, (a, b) -> a));

			return authors.stream()
					.map(entityMap::get)
					.filter(Objects::nonNull)
					.map(authorMongoMapper::entity2Domain)
					.toList();
		}).orElse(Collections.emptyList());
	}
}
