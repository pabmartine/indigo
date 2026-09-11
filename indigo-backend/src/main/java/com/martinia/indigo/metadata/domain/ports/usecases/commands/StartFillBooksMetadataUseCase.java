package com.martinia.indigo.metadata.domain.ports.usecases.commands;

import com.martinia.indigo.metadata.domain.model.BookMetadataScope;
import com.martinia.indigo.metadata.domain.model.DynamicMetadataPolicy;
import com.martinia.indigo.metadata.domain.model.MetadataMergePolicy;

public interface StartFillBooksMetadataUseCase {

	default void start(BookMetadataScope scope, MetadataMergePolicy mergePolicy, DynamicMetadataPolicy dynamicPolicy) {
		start(scope, mergePolicy, dynamicPolicy, 0);
	}

	void start(BookMetadataScope scope, MetadataMergePolicy mergePolicy, DynamicMetadataPolicy dynamicPolicy, long runId);
}
