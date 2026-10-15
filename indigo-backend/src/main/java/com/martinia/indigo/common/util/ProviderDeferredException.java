package com.martinia.indigo.common.util;

import java.time.Instant;
import java.util.Collections;
import java.util.IdentityHashMap;

/** The provider cannot be queried yet; callers should continue with other work. */
public final class ProviderDeferredException extends RuntimeException {
    private final Instant retryAt;
    private final Integer httpStatus;

    public ProviderDeferredException(Instant retryAt, Integer httpStatus) {
        super("Metadata provider deferred until " + retryAt);
        this.retryAt = retryAt;
        this.httpStatus = httpStatus;
    }

    public Instant retryAt() { return retryAt; }
    public Integer httpStatus() { return httpStatus; }

    public static boolean causedBy(Throwable error) {
        var visited = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        for (Throwable cause = error; cause != null && visited.add(cause); cause = cause.getCause()) {
            if (cause instanceof ProviderDeferredException) return true;
        }
        return false;
    }
}
