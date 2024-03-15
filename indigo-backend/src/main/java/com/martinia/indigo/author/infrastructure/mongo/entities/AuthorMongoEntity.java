package com.martinia.indigo.author.infrastructure.mongo.entities;

import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "authors")
public class AuthorMongoEntity implements Serializable {

	@Id
	private String id;
	private String name;
	private String sort;
	private String description;
	private String provider;
	private String image;
	private NumBooksMongo numBooks;

	private Date lastMetadataSync;

}
