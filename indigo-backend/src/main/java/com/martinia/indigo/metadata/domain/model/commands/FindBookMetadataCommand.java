package com.martinia.indigo.metadata.domain.model.commands;

import com.martinia.indigo.common.bus.command.domain.model.Command;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.model.DynamicMetadataPolicy;
import com.martinia.indigo.metadata.domain.model.MetadataMergePolicy;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindBookMetadataCommand extends Command<MetadataItemResult> {
	private String bookId;
	private MetadataMergePolicy mergePolicy;
	private DynamicMetadataPolicy dynamicPolicy;
	private long lastExecution;
}
