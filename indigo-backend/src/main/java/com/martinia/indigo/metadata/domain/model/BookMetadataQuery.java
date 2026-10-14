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
        return java.util.stream.Stream.concat(
                isbn13 == null ? java.util.stream.Stream.empty() : isbn13.stream(),
                isbn10 == null ? java.util.stream.Stream.empty() : isbn10.stream())
                .filter(java.util.Objects::nonNull)
                .map(value -> value.replaceAll("[\\s-]", ""))
                .filter(value -> !value.isBlank())
                .findFirst().orElse(null);
    }
}
