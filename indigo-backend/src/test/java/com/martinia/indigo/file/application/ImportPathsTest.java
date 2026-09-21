package com.martinia.indigo.file.application;

import java.io.IOException;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ImportPathsTest {
    @TempDir Path root;

    @Test void rejectsLibraryFilesAndSymbolicLinksWithNestedUploads() throws Exception {
        Path library = Files.createDirectory(root.resolve("library"));
        Path uploads = Files.createDirectory(library.resolve("uploads"));
        Path installed = Files.writeString(library.resolve("book.epub"), "original");
        assertThrows(IOException.class, () -> ImportPaths.upload(installed, uploads, library));
        Path link = Files.createSymbolicLink(uploads.resolve("book.epub"), installed);
        assertThrows(IOException.class, () -> ImportPaths.upload(link, uploads, library));
        Path folderLink = Files.createSymbolicLink(uploads.resolve("folder"), library);
        assertThrows(IOException.class, () -> ImportPaths.upload(folderLink.resolve("book.epub"), uploads, library));
        assertThrows(IOException.class, () -> ImportPaths.libraryDirectory(uploads, uploads, library));
        assertEquals("original", Files.readString(installed));
    }

    @Test void rejectsUploadRootsThatContainTheLibrary() throws Exception {
        Path library = Files.createDirectory(root.resolve("library"));
        Path file = Files.writeString(library.resolve("book.epub"), "original");
        assertThrows(IOException.class, () -> ImportPaths.upload(file, library, library));
        assertThrows(IOException.class, () -> ImportPaths.upload(file, root, library));
    }
}
