package com.martinia.indigo.adapters.out.mongo.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import com.martinia.indigo.adapters.out.mongo.beans.NumBooksMongoBean;

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
	private NumBooksMongoBean numBooks;
	
	public TagMongoEntity(String tag, List<String> languages) {
		this.name = tag;
		this.numBooks = new NumBooksMongoBean();
		this.numBooks.setTotal(1);
		for (String lang : languages) {
			this.numBooks.getLanguages().put(lang, 1);
		}
	}
	
}
