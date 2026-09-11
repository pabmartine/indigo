package com.martinia.indigo.book.domain.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.martinia.indigo.common.domain.model.Review;
import com.martinia.indigo.common.domain.model.Serie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book implements Serializable {

	private String id;
	private String title;
	private String path;
	private String comment;
	private String provider;
	private Serie serie;
	private Date pubDate;
	private Date lastModified;
	private int pages;
	private float rating;
	private String image;
	private List<String> authors;
	private List<String> tags;
	private List<String> isbn10;
	private List<String> isbn13;
	private Map<String, List<String>> identifiers;
	private String openLibraryWorkId;
	private String openLibraryEditionId;
	private List<String> similar;
	private List<String> recommendations;
	private List<String> languages;
	private List<Review> reviews;
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

	public Book(final String id, final String title, final String path, final String comment, final String provider,
			final Serie serie, final Date pubDate, final Date lastModified, final int pages, final float rating,
			final String image, final List<String> authors, final List<String> tags, final List<String> similar,
			final List<String> recommendations, final List<String> languages, final List<Review> reviews,
			final Date lastMetadataSync, final float version) {
		this.id = id;
		this.title = title;
		this.path = path;
		this.comment = comment;
		this.provider = provider;
		this.serie = serie;
		this.pubDate = pubDate;
		this.lastModified = lastModified;
		this.pages = pages;
		this.rating = rating;
		this.image = image;
		this.authors = authors;
		this.tags = tags;
		this.similar = similar;
		this.recommendations = recommendations;
		this.languages = languages;
		this.reviews = reviews;
		this.lastMetadataSync = lastMetadataSync;
		this.version = version;
	}
}
