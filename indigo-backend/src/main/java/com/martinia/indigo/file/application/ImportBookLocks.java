package com.martinia.indigo.file.application;

/** Bounded locks: versions, ISBN/title aliases and colliding destination names must finish in order. */
public final class ImportBookLocks {
    private static final Object[] LOCKS = new Object[1024];
    static { java.util.Arrays.setAll(LOCKS, ignored -> new Object()); }
    private ImportBookLocks() { }
    public static Object forTitle(String title) {
        title = title == null ? null : title.trim();
        String key = EpubImportPolicy.title(title == null || title.isBlank() || title.equals(".") || title.equals("..")
                ? "Unknown" : title);
        return LOCKS[Math.floorMod(key.hashCode(), LOCKS.length)];
    }
}
