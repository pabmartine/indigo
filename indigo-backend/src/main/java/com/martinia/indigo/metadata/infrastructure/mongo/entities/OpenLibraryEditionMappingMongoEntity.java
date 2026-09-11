package com.martinia.indigo.metadata.infrastructure.mongo.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "openLibraryEditionMappings")
@CompoundIndexes({
		@CompoundIndex(name = "ol_mapping_version_isbn", def = "{'indexVersion': 1, 'isbn': 1}"),
		@CompoundIndex(name = "ol_mapping_version_work", def = "{'indexVersion': 1, 'workId': 1}")
})
public class OpenLibraryEditionMappingMongoEntity {
	@Id
	private String id;
	private String indexVersion;
	private String isbn;
	private String editionId;
	private String workId;
}
