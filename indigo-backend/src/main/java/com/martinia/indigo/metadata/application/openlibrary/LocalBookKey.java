package com.martinia.indigo.metadata.application.openlibrary;

import com.martinia.indigo.metadata.application.BookMetadataTitles;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/** Exact normalized title AND author; never join unrelated books on title alone. */
public final class LocalBookKey {
    private LocalBookKey() {}
    public static Set<String> keys(String title, Collection<String> authors) {
        String normalized = BookMetadataTitles.normalize(title);
        Set<String> keys = new LinkedHashSet<>();
        if (normalized.isBlank() || authors == null) return keys;
        for (String author : authors) {
            String name = AuthorNameNormalizer.normalize(author);
            if (!name.isBlank()) keys.add(normalized + "|" + name);
        }
        return keys;
    }
}
