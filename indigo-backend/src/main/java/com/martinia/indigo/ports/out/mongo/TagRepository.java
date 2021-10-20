package com.martinia.indigo.ports.out.mongo;

import java.util.List;

import com.martinia.indigo.domain.model.Tag;

public interface TagRepository {


	public List<Tag> findAll(String sort, String order);

	public Tag findByName(String name);

	public void image(String source, String image);

	public void merge(String source, String target);

	public void rename(String source, String target);

	public void save(List<String> tags);

	void dropCollection();

}
