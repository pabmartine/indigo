package com.martinia.indigo.services.calibre;

import java.util.List;

import com.martinia.indigo.model.calibre.Tag;

public interface TagService {

	Tag findByName(String name);

	List<String> getTagsByBookId(int id);

	Tag getTagByName(String name);

	long count();

	List<Object[]> getNumBooksByTag(String sort, String order);

	void rename(int source, String target);

	void merge(int source, int target);

}
