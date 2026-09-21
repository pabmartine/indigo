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

	/** Drop region, script, encoding and locale modifiers, retaining the base language. */
	public static String normalize(String language) {
		if (language == null) return null;
		String code = language.trim().toLowerCase(Locale.ROOT);
		return code.split("[_.@\\s-]", 2)[0];
	}

	public static java.util.List<String> normalizeAll(java.util.Collection<String> languages) {
		if (languages == null) return java.util.List.of();
		return languages.stream().map(LanguageCodeUtils::normalize)
				.filter(code -> code != null && !code.isBlank()).distinct().toList();
	}

	public static java.util.List<String> expand(java.util.Collection<String> languages) {
		return normalizeAll(languages).stream().flatMap(language -> variants(language).stream()).distinct().toList();
	}

	public static Set<String> variants(final String language) {
		if (language == null || language.isBlank()) {
			return Set.of();
		}

		final String code = normalize(language);
		if (code.isBlank()) return Set.of();
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
