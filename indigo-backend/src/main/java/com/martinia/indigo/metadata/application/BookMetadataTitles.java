package com.martinia.indigo.metadata.application;

import org.apache.commons.lang3.StringUtils;

/** Accept edition annotations, but do not mistake a sequel for a title prefix match. */
public final class BookMetadataTitles {
    private BookMetadataTitles() {}

    public static String normalize(String title) {
        return StringUtils.stripAccents(StringUtils.defaultString(title)
                .replaceAll("\\s*\\([^()]*\\)\\s*$", ""))
                .replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT).trim();
    }

    public static boolean matches(String expected, String actual, String subtitle) {
        String title = normalize(expected);
        return !title.isBlank() && (title.equals(normalize(actual))
                || (StringUtils.isNotBlank(subtitle) && title.equals(normalize(actual + " " + subtitle))));
    }
}
