package com.martinia.indigo.common.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

public final class LanguageCodeUtils {

	private static final java.util.concurrent.ConcurrentMap<String, Set<String>> CACHE = new java.util.concurrent.ConcurrentHashMap<>();

	private LanguageCodeUtils() {
	}

	public static Set<String> variants(final String language) {
		if (language == null || language.isBlank()) {
			return Set.of();
		}

		final String code = language.trim().toLowerCase(Locale.ROOT);
		return CACHE.computeIfAbsent(code, key -> {
			final Set<String> variants = new LinkedHashSet<>();
			variants.add(key);
			for (Locale locale : Locale.getAvailableLocales()) {
				try {
					if (key.equals(locale.getLanguage()) || key.equals(locale.getISO3Language())) {
						variants.add(locale.getLanguage());
						variants.add(locale.getISO3Language());
					}
				}
				catch (MissingResourceException ignored) {
					// Ignore locales without an ISO-639 mapping.
				}
			}
			return Collections.unmodifiableSet(variants);
		});
	}
}
