package com.martinia.indigo.serie.infrastructure.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Paginated series response")
public record SeriePageDto(
		@Schema(description = "Series items for the requested page") List<Map<String, String>> items,
		@Schema(description = "Total number of series matching the filter", example = "120") long total,
		@Schema(description = "Current page index", example = "0") int page,
		@Schema(description = "Requested page size", example = "20") int size
) {
}
