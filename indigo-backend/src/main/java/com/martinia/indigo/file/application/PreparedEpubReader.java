package com.martinia.indigo.file.application;

import com.martinia.indigo.common.domain.model.BookOpf;
import com.martinia.indigo.common.util.ImageUtils;
import com.martinia.indigo.common.util.XmlUtils;
import jakarta.annotation.Resource;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** One ZIP directory read, with lazy images: duplicate/version decisions need only the OPF. */
@Service
public class PreparedEpubReader {
    @Value("${book.library.uploads}") private String uploads;
    @Resource private ImageUtils images;

    public Prepared read(Path input) throws IOException {
        Path root = Path.of(uploads).toRealPath();
        Path source = input.toRealPath();
        if (!source.startsWith(root) || !Files.isRegularFile(source)) throw new IOException("EPUB outside upload directory");
        Path folder = Files.createTempDirectory(root, ".import-");
        Path path = Files.move(source, folder.resolve(source.getFileName()));
        ZipFile zip = new ZipFile(path.toFile());
        try {
            List<? extends ZipEntry> entries = zip.stream().filter(e -> !e.isDirectory()).toList();
            ZipEntry opfEntry = entries.stream().filter(e -> e.getName().toLowerCase(Locale.ROOT).endsWith(".opf"))
                    .findFirst().orElseThrow(() -> new IOException("EPUB without OPF"));
            BookOpf opf;
            try (InputStream stream = zip.getInputStream(opfEntry)) { opf = XmlUtils.parse(stream); }
            if (opf == null) throw new IOException("Invalid EPUB metadata");
            return new Prepared(path, opf, zip, entries);
        } catch (Exception error) { zip.close(); throw new IOException("Could not prepare EPUB " + path, error); }
    }

    public final class Prepared implements AutoCloseable {
        private final Path path;
        private final BookOpf opf;
        private final ZipFile zip;
        private final List<? extends ZipEntry> entries;
        private Prepared(Path path, BookOpf opf, ZipFile zip, List<? extends ZipEntry> entries) {
            this.path = path; this.opf = opf; this.zip = zip; this.entries = entries;
        }
        public Path path() { return path; }
        public BookOpf opf() { return opf; }
        public void loadImages() {
            try {
                byte[] cover = image(Optional.ofNullable(opf.getBookImageName()).orElse("cover.jpg"));
                if (cover != null) opf.setBookImage(images.saveCoverAndGetThumbnail(cover, path.getParent().resolve("cover.jpg")));
            } catch (Exception ex) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("Could not prepare cover for {}", path, ex); }
            try {
                byte[] author = image(Optional.ofNullable(opf.getAuthorImageName()).orElse("autor.jpg"));
                if (author != null) opf.setAuthorImage(images.getBase64Cover(new ByteArrayInputStream(author), true));
            } catch (Exception ex) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("Could not prepare author image for {}", path, ex); }
        }
        private byte[] image(String name) throws IOException {
            var entry = entries.stream().filter(e -> e.getName().toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))).findFirst();
            if (entry.isEmpty()) return null;
            try (InputStream stream = zip.getInputStream(entry.get())) { return stream.readAllBytes(); }
        }
        @Override public void close() throws IOException { zip.close(); }
    }
}
