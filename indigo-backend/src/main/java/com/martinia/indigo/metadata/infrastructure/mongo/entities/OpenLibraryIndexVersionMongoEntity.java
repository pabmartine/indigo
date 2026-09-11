package com.martinia.indigo.metadata.infrastructure.mongo.entities;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "openLibraryIndexVersions")
public class OpenLibraryIndexVersionMongoEntity {
	@Id
	private String version;
	@Indexed
	private boolean active;
	private Date createdAt;
	private Date activatedAt;
	private String editionsSource;
	private String ratingsSource;
	private String editionsLastModified;
	private String ratingsLastModified;
	private long libraryIsbns;
	private long matchedIsbns;
	private long matchedWorks;
	private long worksWithRatings;
	private long processedEditions;
	private long processedRatings;
}
