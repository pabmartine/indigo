package com.martinia.indigo.book.domain.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.martinia.indigo.common.model.Review;
import com.martinia.indigo.common.model.Serie;

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
	private List<String> similar;
	private List<String> recommendations;
	private List<String> languages;
	private List<Review> reviews;

	private Date lastMetadataSync;
}
