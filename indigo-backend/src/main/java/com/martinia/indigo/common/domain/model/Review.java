package com.martinia.indigo.common.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Review {
	public Review(String name, String title, String comment, int rating, Date date, Date lastMetadataSync, String provider) {
		this(name, title, comment, rating, date, lastMetadataSync, provider, null, null, null);
	}
	private String name;
	private String title;
	private String comment;
	private int rating;
	private Date date;
	private Date lastMetadataSync;

	private String provider;
	private String sourceUrl;
	private String originalLanguage;
	private String language;

}
