package com.martinia.indigo.file.application;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.assertThat;

class EpubFileScannerTest {
    @TempDir Path root;

    @Test void nativeAndPortableScanAgreeOnNestedFilesAndNeverFollowLinks() throws Exception {
        Path uploads = Files.createDirectory(root.resolve("uploads"));
        Path nested = Files.createDirectory(uploads.resolve("directory.epub"));
        Path one = Files.createFile(uploads.resolve("quotes ' and \" and\nnewline.EpUb"));
        Path two = Files.createFile(nested.resolve("niño.epub"));
        Files.createFile(uploads.resolve("ignore.txt"));
        Path outside = Files.createDirectory(root.resolve("outside"));
        Files.createFile(outside.resolve("outside.epub"));
        Files.createSymbolicLink(uploads.resolve("linked-directory"), outside);
        Files.createSymbolicLink(uploads.resolve("linked.epub"), one);
        Files.createSymbolicLink(uploads.resolve("broken.epub"), root.resolve("missing"));
        var scanner = new EpubFileScanner();
        assertThat(scanner.scan(uploads)).containsExactlyInAnyOrder(one, two);
        assertThat(scanner.javaScan(uploads)).containsExactlyInAnyOrder(one, two);
    }
}
