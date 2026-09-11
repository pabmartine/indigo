package com.martinia.indigo.file.application;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/** Book identity is independent of the filename and the EPUB bytes. */
public final class EpubImportPolicy {
    private EpubImportPolicy() { }

    public static String title(String value) {
        return StringUtils.stripAccents(StringUtils.defaultString(value)).toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    @SafeVarargs
    public static Set<String> isbnKeys(Collection<String>... collections) {
        Set<String> keys = new LinkedHashSet<>();
        for (Collection<String> values : collections) {
            if (values == null) continue;
            for (String value : values) {
                if (value == null) continue;
                String isbn = value.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
                if (isbn.matches("[0-9]{9}[0-9X]")) {
                    keys.add(isbn);
                    String prefix = "978" + isbn.substring(0, 9);
                    int sum = 0;
                    for (int i = 0; i < 12; i++) sum += (prefix.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
                    keys.add(prefix + ((10 - sum % 10) % 10));
                } else if (isbn.matches("[0-9]{13}")) {
                    keys.add(isbn);
                    if (isbn.startsWith("978")) {
                        String prefix = isbn.substring(3, 12);
                        int sum = 0;
                        for (int i = 0; i < 9; i++) sum += (prefix.charAt(i) - '0') * (10 - i);
                        int check = (11 - sum % 11) % 11;
                        keys.add(prefix + (check == 10 ? "X" : Integer.toString(check)));
                    }
                }
            }
        }
        return keys;
    }

    public static boolean upgrade(float incoming, float current) {
        return Float.isFinite(incoming) && incoming > 0 && incoming > (Float.isFinite(current) ? current : 0);
    }
}
