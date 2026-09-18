package com.martinia.indigo.tag.domain.model;

import java.io.Serializable;
import java.util.List;

public record TagPageData(
		List<Tag> items,
		long total,
		int page,
		int size
) implements Serializable {
}
