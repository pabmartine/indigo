package com.martinia.indigo.book.infrastructure.mongo.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "books")
public class BookMongoEntity implements Serializable {

	private static final long serialVersionUID = 7913092341598911896L;

	@BsonRepresentation(BsonType.OBJECT_ID)
	@Id
	private String id;
	private String title;
	private String path;
	private String comment;
	private String provider;
	private SerieMongo serie;
	private Date pubDate;
	private Date lastModified;
	private int pages;
	private float rating;
	private String image;
	private List<String> authors;
	private List<String> tags;
	private List<String> similar;
	private List<String> recommendations;
	private List<String> languages;
	private List<ReviewMongo> reviews;

	private Date lastMetadataSync;

}
