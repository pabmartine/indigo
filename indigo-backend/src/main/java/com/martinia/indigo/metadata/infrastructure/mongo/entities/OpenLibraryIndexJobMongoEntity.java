package com.martinia.indigo.metadata.infrastructure.mongo.entities;

import java.util.Date;

import com.martinia.indigo.metadata.domain.model.OpenLibraryIndexJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "openLibraryIndexJobs")
public class OpenLibraryIndexJobMongoEntity {
	@Id
	private String id;
	private OpenLibraryIndexJobStatus status;
	private String stagingVersion;
	private String activeVersion;
	private String editionsUrl;
	private String ratingsUrl;
	private String editionsFile;
	private String ratingsFile;
	private long downloadedBytes;
	private long totalBytes;
	private long processedRecords;
	private long matchedRecords;
	private long libraryIsbns;
	private long matchedIsbns;
	private long matchedWorks;
	private long worksWithRatings;
	private boolean cancelRequested;
	private boolean deleteDownloadsOnSuccess;
	private String editionsLastModified;
	private String ratingsLastModified;
	private String error;
	private Date requestedAt;
	private Date startedAt;
	private Date updatedAt;
	private Date completedAt;
}
