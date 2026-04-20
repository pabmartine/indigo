package com.martinia.indigo.book.infrastructure.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated lightweight books response")
public record BookSummaryPageDto(
		@Schema(description = "Books items for the requested page") List<BookSummaryDto> items,
		@Schema(description = "Total number of books matching the search", example = "250") long total,
		@Schema(description = "Current page index", example = "0") int page,
		@Schema(description = "Requested page size", example = "20") int size
) {
}
