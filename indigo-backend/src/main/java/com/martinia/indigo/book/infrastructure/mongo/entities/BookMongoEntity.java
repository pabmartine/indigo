package com.martinia.indigo.book.infrastructure.mongo.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "books")
@CompoundIndex(name = "serie_language_idx", def = "{'serie.name': 1, 'languages': 1}")
@CompoundIndex(name = "languages_rating_idx", def = "{'languages': 1, 'rating': -1}")
@CompoundIndex(name = "languages_pubdate_idx", def = "{'languages': 1, 'pubDate': -1}")
@CompoundIndex(name = "authors_languages_idx", def = "{'authors': 1, 'languages': 1}")
@CompoundIndex(name = "tags_languages_idx", def = "{'tags': 1, 'languages': 1}")
public class BookMongoEntity implements Serializable {

	@BsonRepresentation(BsonType.OBJECT_ID)
	@Id
	private String id;
	@TextIndexed
	private String title;
	@Indexed
	private String path;
	private String comment;
	private String provider;
	private SerieMongo serie;
	@Indexed
	private Date pubDate;
	private Date lastModified;
	@Indexed
	private int pages;
	private float rating;
	private String image;
	@TextIndexed
	private List<String> authors;
	@Indexed
	private List<String> tags;
	private List<String> similar;
	private List<String> recommendations;
	@Indexed
	private List<String> languages;
	private List<ReviewMongo> reviews;

	private Date lastMetadataSync;
	private float version;

}
