package com.martinia.indigo.book.infrastructure.api.model;

import java.io.Serializable;
import java.util.List;

import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDto implements Serializable {

	private static final long serialVersionUID = 3010775368721977810L;

	private String id;
	private String title;
	private String path;
	private String comment;
	private String provider;
	private SerieDto serie;
	private String pubDate;
	private String lastModified;
	private int pages;
	private float rating;
	private String image;
	private List<String> authors;
	private List<String> tags;
	private List<String> similar;
	private List<String> recommendations;
	private List<String> languages;
	private List<ReviewDto> reviews;

}