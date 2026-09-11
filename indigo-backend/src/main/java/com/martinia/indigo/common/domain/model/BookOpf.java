package com.martinia.indigo.common.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;
import java.util.Map;

@SuperBuilder
@Data
@NoArgsConstructor
public class BookOpf {
	private String title;
	private String comment;
	private String authorId;
	private List<String> authors;
	private List<String> translators;
	private List<String> tags;
	private List<String> isbn10;
	private List<String> isbn13;
	private Map<String, List<String>> identifiers;
	private String language;
	private Date pubDate;
	private Date lastModified;
	private String seriesName;
	private int seriesIndex;
	private int pages;
	private float version;
	private String bookImageName;
	private String bookImage;
	private String authorImageName;
	private String authorImage;
}
