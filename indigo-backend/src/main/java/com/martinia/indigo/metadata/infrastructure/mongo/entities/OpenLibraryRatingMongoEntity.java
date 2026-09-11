package com.martinia.indigo.metadata.infrastructure.mongo.entities;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "openLibraryRatings")
@CompoundIndex(name = "ol_rating_version_work", def = "{'indexVersion': 1, 'workId': 1}", unique = true)
public class OpenLibraryRatingMongoEntity {
	@Id
	private String id;
	private String indexVersion;
	private String workId;
	private Float average;
	private long count;
	private Map<String, Long> distribution;
}
