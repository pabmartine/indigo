package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.favorite.FindFavoriteAuthorsUseCase;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
			List<Author> ret = new ArrayList<>(authors != null ? authors.size() : 0);
			if (!CollectionUtils.isEmpty(authors)) {
				authors.forEach(author -> ret.add(findAuthor(author).get()));
			}
			return ret;
		}).orElse(Collections.emptyList());

	}

	private Optional<Author> findAuthor(String sort) {
		return authorRepository.findByName(sort)
				.map(author -> Optional.of(authorMongoMapper.entity2Domain(author)))
				.orElse(Optional.empty());
	}

}
