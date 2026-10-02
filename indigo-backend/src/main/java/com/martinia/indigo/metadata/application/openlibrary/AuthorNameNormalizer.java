package com.martinia.indigo.metadata.application.openlibrary;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

public final class AuthorNameNormalizer {
	private AuthorNameNormalizer() {
	}

	public static String normalize(String name) {
		return StringUtils.stripAccents(StringUtils.defaultString(name)).toLowerCase(Locale.ROOT)
				.replaceAll("[^\\p{L}\\p{N}]", " ").replaceAll("\\s+", " ").trim();
	}
}
