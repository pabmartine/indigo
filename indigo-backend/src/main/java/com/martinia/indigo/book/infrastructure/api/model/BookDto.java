package com.martinia.indigo.book.infrastructure.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class BookDto implements Serializable {

	@NotEmpty
	private String id;
	@NotEmpty
	private String title;
	@NotEmpty
	private String path;
	@NotEmpty
	private String comment;
	private String provider;
	private SerieDto serie;
	@NotEmpty
	private String pubDate;
	private String lastModified;
	@NotNull
	private Integer pages;
	private float rating;
	private String image;
	@NotNull
	private List<String> authors;
	private List<String> tags;
	private List<String> similar;
	private List<String> recommendations;
	@NotNull
	private List<String> languages;
	private List<ReviewDto> reviews;

}
