package com.martinia.indigo.ports.out.mongo;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.domain.model.Tag;

public interface TagRepository {


	List<Tag> findAll(List<String> languages, String sort, String order);

	Optional<Tag> findByName(String name);

	void image(String source, String image);

	void merge(String source, String target);

	void rename(String source, String target);

	void save(List<String> tags, List<String> languages);

	void dropCollection();

}
