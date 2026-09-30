package com.martinia.indigo.file.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Enumerates names without opening EPUBs. Native find can use directory entry types instead of stat per file. */
public class EpubFileScanner {
    public List<Path> scan(Path root) throws IOException {
        root = root.toAbsolutePath().normalize();
        Files.createDirectories(root);
        for (String executable : List.of("/usr/bin/find", "/bin/find")) {
            if (Files.isExecutable(Path.of(executable))) return nativeScan(executable, root);
        }
        return javaScan(root);
    }

    private List<Path> nativeScan(String executable, Path root) throws IOException {
        Path errors = Files.createTempFile("indigo-scan-", ".log");
        Process process = null;
        try {
            // No shell; NUL delimiters preserve spaces, quotes and newlines in file names. Never follow symlinks.
            process = new ProcessBuilder(executable, "-P", root.toString(), "-type", "f", "-iname", "*.epub", "-print0")
                    .redirectError(errors.toFile()).start();
            List<Path> result = new ArrayList<>();
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                StringBuilder name = new StringBuilder();
                char[] buffer = new char[16384];
                int count;
                while ((count = reader.read(buffer)) != -1) {
                    for (int i = 0; i < count; i++) {
                        if (buffer[i] == 0) {
                            result.add(Path.of(name.toString()));
                            name.setLength(0);
                        } else name.append(buffer[i]);
                    }
                }
                if (!name.isEmpty()) throw new IOException("Incomplete EPUB directory listing");
            }
            if (process.waitFor() != 0) {
                try (var error = Files.newBufferedReader(errors)) {
                    char[] detail = new char[4096];
                    int count = error.read(detail);
                    throw new IOException("EPUB directory scan failed: " + (count > 0 ? new String(detail, 0, count) : root));
                }
            }
            return result;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IOException("EPUB directory scan interrupted", interrupted);
        } finally {
            if (process != null && process.isAlive()) process.destroyForcibly();
            Files.deleteIfExists(errors);
        }
    }

    List<Path> javaScan(Path root) throws IOException {
        List<Path> result = new ArrayList<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                if (attributes.isRegularFile() && file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".epub")) {
                    result.add(file.toAbsolutePath());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
}
