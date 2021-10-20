package com.martinia.indigo.ports.in.rest;

import java.util.List;

import com.martinia.indigo.domain.model.Tag;


public interface TagService {

	Tag findByName(String name);

//	long count();

	void rename(String source, String target);

	void merge(String source, String target);

	void image(String source, String image);

	List<Tag> findAll(String sort, String order);

}
