package com.martinia.indigo.metadata.domain.model;

import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookMetadataQuery {

	private String title;
	@Builder.Default
	private List<String> authors = Collections.emptyList();
	@Builder.Default
	private List<String> isbn10 = Collections.emptyList();
	@Builder.Default
	private List<String> isbn13 = Collections.emptyList();
	@Builder.Default
	private List<String> languages = Collections.emptyList();
	private Integer publicationYear;

	public String preferredIsbn() {
		if (isbn13 != null && !isbn13.isEmpty()) {
			return isbn13.get(0);
		}
		return isbn10 == null || isbn10.isEmpty() ? null : isbn10.get(0);
	}
}
