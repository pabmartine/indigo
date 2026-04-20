package com.martinia.indigo.metadata.domain.ports.usecases;

import java.util.Map;

public interface FindMetadataSummaryUseCase {

	Map<String, Long> getSummary();

}
