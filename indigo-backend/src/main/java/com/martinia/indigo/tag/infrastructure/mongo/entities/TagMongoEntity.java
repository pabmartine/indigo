package com.martinia.indigo.tag.infrastructure.mongo.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Id;

import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "tags")
public class TagMongoEntity implements Serializable {


	
	private static final long serialVersionUID = 3821856920529740875L;

	@Id
	private String id;
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
