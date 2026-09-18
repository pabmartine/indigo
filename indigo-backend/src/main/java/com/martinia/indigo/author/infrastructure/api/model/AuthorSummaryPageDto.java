package com.martinia.indigo.author.infrastructure.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated lightweight authors response")
public record AuthorSummaryPageDto(
		@Schema(description = "Author items for the requested page") List<AuthorSummaryDto> items,
		@Schema(description = "Total number of authors matching filter", example = "150") long total,
		@Schema(description = "Current page index", example = "0") int page,
		@Schema(description = "Requested page size", example = "20") int size
) {
}
