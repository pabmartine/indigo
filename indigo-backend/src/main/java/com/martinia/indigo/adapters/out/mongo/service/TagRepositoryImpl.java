package com.martinia.indigo.adapters.out.mongo.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.martinia.indigo.adapters.out.mongo.entities.BookMongoEntity;
import com.martinia.indigo.adapters.out.mongo.entities.TagMongoEntity;
import com.martinia.indigo.adapters.out.mongo.mapper.TagMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.BookMongoRepository;
import com.martinia.indigo.adapters.out.mongo.repository.TagMongoRepository;
import com.martinia.indigo.domain.model.Tag;
import com.martinia.indigo.ports.out.mongo.TagRepository;

@Component
public class TagRepositoryImpl implements TagRepository {

	@Autowired
	TagMongoRepository tagMongoRepository;

	@Autowired
	BookMongoRepository bookMongoRepository;

	@Autowired
	TagMongoMapper tagMongoMapper;

	@Override
	public List<Tag> findAll(String sort, String order) {
		return tagMongoMapper
				.entities2Domains(tagMongoRepository.findAll(Sort.by(Sort.Direction.fromString(order), sort)));
	}

	@Override
	public Tag findByName(String name) {
		return tagMongoMapper.entity2Domain(tagMongoRepository.findByName(name));
	}

//	@Override
//	public long count() {
//		return tagMongoRepository.count();
//	}

	@Override
	public void image(String source, String image) {
		TagMongoEntity entity = tagMongoRepository.findById(source)
				.get();
		entity.setImage(image);
		tagMongoRepository.save(entity);
	}

	@Override
	public void merge(String source, String target) {
		TagMongoEntity entitySource = tagMongoRepository.findById(source)
				.get();
		TagMongoEntity entityTarget = tagMongoRepository.findById(target)
				.get();

		List<BookMongoEntity> books = bookMongoRepository.findByTag(entitySource.getName());
		for (BookMongoEntity book : books) {
			book.getTags()
					.replaceAll(b -> b.equals(entitySource.getName()) ? entityTarget.getName() : b);
			book.setTags(new ArrayList<>(new LinkedHashSet<>(book.getTags())));
		}
		bookMongoRepository.saveAll(books);

		entityTarget.setNumBooks(entityTarget.getNumBooks() + entitySource.getNumBooks());
		tagMongoRepository.save(entityTarget);
		tagMongoRepository.delete(entitySource);

	}

	@Override
	public void rename(String source, String target) {

		TagMongoEntity entity = tagMongoRepository.findById(source)
				.get();

		List<BookMongoEntity> books = bookMongoRepository.findByTag(entity.getName());
		for (BookMongoEntity book : books) {
			book.getTags()
					.replaceAll(b -> b.equals(entity.getName()) ? target : b);
		}
		bookMongoRepository.saveAll(books);

		entity.setName(target);
		tagMongoRepository.save(entity);

	}

	@Override
	public void save(List<String> tags) {
		for (String tag : tags) {
			TagMongoEntity entity = tagMongoRepository.findByName(tag);
			if (entity == null) {
				tagMongoRepository.save(new TagMongoEntity(tag));
			} else {
				entity.setNumBooks(entity.getNumBooks() + 1);
				tagMongoRepository.save(entity);
			}
		}
	}

	@Override
	public void dropCollection() {
		tagMongoRepository.dropCollection(TagMongoEntity.class);
	}

}
