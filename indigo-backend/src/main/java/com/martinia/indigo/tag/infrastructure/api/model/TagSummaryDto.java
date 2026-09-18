package com.martinia.indigo.tag.infrastructure.api.model;

import java.io.Serializable;

public record TagSummaryDto(
		String id,
		String name,
		int numBooks
) implements Serializable {
}
