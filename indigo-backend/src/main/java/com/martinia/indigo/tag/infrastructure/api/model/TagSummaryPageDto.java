package com.martinia.indigo.tag.infrastructure.api.model;

import java.io.Serializable;
import java.util.List;

public record TagSummaryPageDto(
		List<TagSummaryDto> items,
		long total,
		int page,
		int size
) implements Serializable {
}
