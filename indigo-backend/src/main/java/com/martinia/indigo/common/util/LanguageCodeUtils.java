package com.martinia.indigo.common.util;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

public final class LanguageCodeUtils {

	private LanguageCodeUtils() {
	}

	public static Set<String> variants(final String language) {
		final Set<String> variants = new LinkedHashSet<>();
		if (language == null || language.isBlank()) {
			return variants;
		}

		final String code = language.trim().toLowerCase(Locale.ROOT);
		variants.add(code);
		for (Locale locale : Locale.getAvailableLocales()) {
			try {
				if (code.equals(locale.getLanguage()) || code.equals(locale.getISO3Language())) {
					variants.add(locale.getLanguage());
					variants.add(locale.getISO3Language());
				}
			}
			catch (MissingResourceException ignored) {
				// Ignore locales without an ISO-639 mapping.
			}
		}
		return variants;
	}
}
