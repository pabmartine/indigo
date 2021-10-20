package com.martinia.indigo.adapters.out.mongo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.martinia.indigo.adapters.out.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.adapters.out.mongo.mapper.AuthorMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.AuthorMongoRepository;
import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.ports.out.mongo.AuthorRepository;

@Component
public class AuthorRepositoryImpl implements AuthorRepository {

	@Autowired
	AuthorMongoRepository authorMongoRepository;

	@Autowired
	AuthorMongoMapper authorMongoMapper;

	@Override
	public Long count() {
		return authorMongoRepository.count();
	}

	@Override
	public List<Author> findAll(int page, int size, String sort, String order) {
		return authorMongoMapper.entities2Domains(authorMongoRepository
				.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sort)))
				.getContent());
	}

	@Override
	public Author findById(String id) {
		return authorMongoMapper.entity2Domain(authorMongoRepository.findById(id)
				.get());
	}

	public Author findByName(String name) {
		return authorMongoMapper.entity2Domain(authorMongoRepository.findByName(name));
	}

	public Author findBySort(String sort) {
		return authorMongoMapper.entity2Domain(authorMongoRepository.findBySort(sort));
	}

	@Override
	public void save(Author author) {

		AuthorMongoEntity mapping = authorMongoMapper.domain2Entity(author);
		AuthorMongoEntity entity = authorMongoRepository.findByName(author.getName());
		if (entity != null) {
			mapping.setId(entity.getId());
			mapping.setNumBooks(mapping.getNumBooks() + 1);
		}
		authorMongoRepository.save(mapping);

	}

	@Override
	public void dropCollection() {
		authorMongoRepository.dropCollection(AuthorMongoEntity.class);
	}

}
