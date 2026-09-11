package com.martinia.indigo.metadata.domain.ports.usecases.commands;

import com.martinia.indigo.metadata.domain.model.DynamicMetadataPolicy;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.model.MetadataMergePolicy;

public interface FindBookMetadataUseCase {

	MetadataItemResult find(String bookId, MetadataMergePolicy mergePolicy, DynamicMetadataPolicy dynamicPolicy,
			long lastExecution);
}
