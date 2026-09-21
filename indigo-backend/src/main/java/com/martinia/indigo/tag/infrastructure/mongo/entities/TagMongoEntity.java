package com.martinia.indigo.tag.infrastructure.mongo.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Id;

import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import lombok.Builder;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "tags")
@CompoundIndex(name = "tag_name_idx", def = "{'name': 1}")
@CompoundIndex(name = "tag_numbooks_total_idx", def = "{'numBooks.total': -1}")
public class TagMongoEntity implements Serializable {

	@Id
	private String id;
	@Indexed
	private String name;
	private String image;
	private NumBooksMongo numBooks;

	public TagMongoEntity(String tag, List<String> languages) {
		this.name = tag;
		this.numBooks = new NumBooksMongo();
		this.numBooks.setTotal(1);
		languages.forEach(lang -> this.numBooks.getLanguages().put(lang, 1));
	}

}
