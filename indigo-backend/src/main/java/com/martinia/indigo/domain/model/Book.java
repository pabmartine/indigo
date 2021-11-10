package com.martinia.indigo.domain.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.martinia.indigo.domain.model.inner.Serie;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Book implements Serializable {

	private static final long serialVersionUID = 7913092341598911896L;

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

}
