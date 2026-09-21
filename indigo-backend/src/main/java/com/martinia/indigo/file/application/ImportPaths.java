package com.martinia.indigo.file.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Uploads may be a dedicated child of the library, but never its root or ancestor. */
public final class ImportPaths {
    private ImportPaths() { }

    public static Path upload(Path source, Path uploads, Path library) throws IOException {
        Path uploadRoot = uploads.toRealPath();
        Path libraryRoot = library.toRealPath();
        if (libraryRoot.startsWith(uploadRoot)) throw new IOException("Uploads must not contain the library");
        Path normalized = source.toAbsolutePath().normalize();
        for (Path current = normalized; current != null; current = current.getParent()) {
            if (Files.isSymbolicLink(current)) throw new IOException("Symbolic link in upload path");
        }
        // Recovery can run after the source was removed, so resolve the existing parent.
        Path real = normalized.getParent().toRealPath().resolve(normalized.getFileName());
        if (real.equals(uploadRoot) || !real.startsWith(uploadRoot)
                || (Files.exists(real) && !Files.isRegularFile(real))) {
            throw new IOException("File outside the dedicated upload directory");
        }
        return real;
    }

    public static void libraryDirectory(Path directory, Path uploads, Path library) throws IOException {
        Path real = directory.toRealPath();
        if (!real.startsWith(library.toRealPath()) || real.startsWith(uploads.toRealPath())) {
            throw new IOException("Destination outside library or inside uploads");
        }
    }
}
