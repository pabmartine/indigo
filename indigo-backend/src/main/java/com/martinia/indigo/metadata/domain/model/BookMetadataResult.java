package com.martinia.indigo.metadata.domain.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookMetadataResult {

	private Float ratingAverage;
	private Long ratingsCount;
	private Map<String, Long> ratingDistribution;
	private String provider;
	private String openLibraryWorkId;
	private String openLibraryEditionId;
	private Double matchConfidence;
}
