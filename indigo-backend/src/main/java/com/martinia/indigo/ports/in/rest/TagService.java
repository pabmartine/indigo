package com.martinia.indigo.ports.in.rest;

import java.util.List;

import com.martinia.indigo.domain.model.Tag;


public interface TagService {

	Tag findByName(String name);

	void rename(String source, String target);

	void merge(String source, String target);

	void image(String source, String image);

	List<Tag> findAll(List<String> languages, String sort, String order);

}
