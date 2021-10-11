package com.martinia.indigo.services.calibre;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.model.calibre.Tag;

public interface TagService {

	Tag findByName(String name);

	void updateAllBookReferences(int sourceId, int targetId);

	List<String> getTagsByBookId(int id);

	Tag getTagByName(String name);

	long count();

	List<Object[]> getNumBooksByTag(String sort, String order);

	Optional<Tag> findById(int source);

	void save(Tag tag);

	void delete(Tag tag);

}
