package com.martinia.indigo.adapters.in.rest.dtos;

import java.io.Serializable;
import java.util.List;

import com.martinia.indigo.domain.model.inner.Serie;

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
	private Serie serie;
	private String pubDate;
	private String lastModified;
	private int pages;
	private float rating;
	private List<String> authors;
	private List<String> tags;
	private List<String> similar;
	private List<String> languages;

}