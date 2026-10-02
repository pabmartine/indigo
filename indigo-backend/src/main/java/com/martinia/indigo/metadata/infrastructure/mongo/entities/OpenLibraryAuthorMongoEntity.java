package com.martinia.indigo.metadata.infrastructure.mongo.entities;

import java.util.List;
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
@Document(collection = "openLibraryAuthors")
@CompoundIndex(name = "ol_author_version_name", def = "{'indexVersion': 1, 'names': 1}")
public class OpenLibraryAuthorMongoEntity {
	@Id
	private String id;
	private String indexVersion;
	private String authorId;
	private List<String> names;
	private String biography;
	private boolean hasPhoto;
}
