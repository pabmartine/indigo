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
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "books")
@CompoundIndex(name = "serie_language_idx", def = "{'serie.name': 1, 'languages': 1}")
@CompoundIndex(name = "serie_name_index_idx", def = "{'serie.name': 1, 'serie.index': 1}")
@CompoundIndex(name = "languages_rating_idx", def = "{'languages': 1, 'rating': -1}")
@CompoundIndex(name = "languages_pubdate_idx", def = "{'languages': 1, 'pubDate': -1}")


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
	@Indexed
	private List<String> isbn10;
	@Indexed
	private List<String> isbn13;
	private Map<String, List<String>> identifiers;
	@Indexed
	private String openLibraryWorkId;
	@Indexed
	private String openLibraryEditionId;
	private List<String> similar;
	private List<String> recommendations;
	@Indexed
	private List<String> languages;
	private List<ReviewMongo> reviews;

	private Date lastMetadataSync;
	private Float ratingAverage;
	private Long ratingsCount;
	private Map<String, Long> ratingDistribution;
	private String ratingProvider;
	private Date ratingUpdatedAt;
	private String metadataMatchStatus;
	private Double metadataMatchConfidence;
	private Date lastReviewsMetadataSync;
	private String reviewsMetadataStatus;
	private String reviewsMetadataError;
	private float version;

}
