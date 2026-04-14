package com.martinia.indigo.book.infrastructure.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookSummaryDto implements Serializable {

	private String id;
	private String title;
	private String path;
	private SerieDto serie;
	private String pubDate;
	private Integer pages;
	private float rating;
	private List<String> authors;
	private List<String> tags;
	private List<String> languages;
}
