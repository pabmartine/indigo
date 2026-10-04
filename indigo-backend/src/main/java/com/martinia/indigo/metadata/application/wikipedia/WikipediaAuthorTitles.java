package com.martinia.indigo.metadata.application.wikipedia;

import com.martinia.indigo.metadata.application.openlibrary.AuthorNameNormalizer;

final class WikipediaAuthorTitles {
    private WikipediaAuthorTitles() {}

    static String normalize(String value) {
        return AuthorNameNormalizer.normalize(value);
    }

    static boolean matches(String name, String title) {
        String expected = normalize(name);
        if (expected.isEmpty()) return false;
        if (expected.equals(normalize(title))) return true;
        // Accept a disambiguation suffix, never an additional surname or Jr./Sr.
        return title != null && title.endsWith(")") && title.contains(" (")
                && expected.equals(normalize(title.substring(0, title.lastIndexOf(" ("))));
    }
}
