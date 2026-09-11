package com.martinia.indigo.file.application;

import com.martinia.indigo.file.domain.model.events.EpubFileAddedEvent;

/** Same-thread scope spanning the save transaction and its after-commit callback. */
public final class ImportExecution implements AutoCloseable {
    private static final ThreadLocal<ImportExecution> CURRENT = new ThreadLocal<>();
    private final Runnable images;
    private EpubFileAddedEvent event;
    public ImportExecution(Runnable images) { this.images = images; CURRENT.set(this); }
    public static void prepareNewBookImages() { var scope = CURRENT.get(); if (scope != null) scope.images.run(); }
    public static boolean capture(EpubFileAddedEvent event) {
        var scope = CURRENT.get();
        if (scope == null) return false;
        scope.event = event; return true;
    }
    public EpubFileAddedEvent event() { return event; }
    @Override public void close() { CURRENT.remove(); }
}
