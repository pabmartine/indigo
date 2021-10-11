package com.martinia.indigo.repository.calibre;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.calibre.Tag;

@Repository
public interface TagRepository extends CrudRepository<Tag, Integer>, CustomTagRepository {

	Tag findByName(String name);

	@Modifying
	@Query("update BooksTagsLink set tag = :targetId where tag = :sourceId")
	void updateAllBookReferences(int sourceId, int targetId);
	
	@Query("select t.name from Tag t, BooksTagsLink l where t.id = l.tag and l.book = :id")
	List<String> getTagsByBookId(int id);
	
	Tag findTagByName(String name);

}
