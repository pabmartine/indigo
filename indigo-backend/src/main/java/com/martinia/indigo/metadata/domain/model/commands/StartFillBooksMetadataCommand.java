package com.martinia.indigo.metadata.domain.model.commands;

import com.martinia.indigo.common.bus.command.domain.model.Command;
import com.martinia.indigo.metadata.domain.model.BookMetadataScope;
import com.martinia.indigo.metadata.domain.model.DynamicMetadataPolicy;
import com.martinia.indigo.metadata.domain.model.MetadataMergePolicy;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StartFillBooksMetadataCommand extends Command<Void> {
	private BookMetadataScope scope;
	private MetadataMergePolicy mergePolicy;
	private DynamicMetadataPolicy dynamicPolicy;
	private long runId;
}
