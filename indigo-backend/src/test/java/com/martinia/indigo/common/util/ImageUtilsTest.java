package com.martinia.indigo.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilsTest {

    private ImageUtils imageUtils;

    @TempDir
    Path tempDir;

    private File epubFile;
    private final String bookDirectory = "book";

    @BeforeEach
    void setUp() throws IOException {
        imageUtils = new ImageUtils();
        ReflectionTestUtils.setField(imageUtils, "libraryPath", tempDir.toString());

        Path bookPath = tempDir.resolve(bookDirectory);
        Files.createDirectories(bookPath);

        Path epubPath = bookPath.resolve("test.epub");
        try (InputStream inputStream = ImageUtilsTest.class.getResourceAsStream("/test.epub")) {
            assertNotNull(inputStream, "Could not find test.epub in resources");
            Files.copy(inputStream, epubPath, StandardCopyOption.REPLACE_EXISTING);
        }
        epubFile = epubPath.toFile();
    }

    @Test
    void getImageFromEpub_WhenFileExists_ShouldReturnBase64String() {
        String result = imageUtils.getImageFromEpub(bookDirectory, "cover");
        assertNotNull(result);
    }

    @Test
    void getEpub_WhenFileExists_ShouldReturnFile() throws IOException {
        Resource result = imageUtils.getEpub(bookDirectory);
        assertNotNull(result);
        assertEquals(epubFile.getName(), result.getFilename());
    }

    @Test
    void getEpub_WhenFileDoesNotExist_ShouldReturnNull() {
        Resource result = imageUtils.getEpub("nonexistent");
        assertNull(result);
    }

    @Test
    void getImageFromEpub_WhenFileDoesNotExist_ShouldReturnNull() {
        String result = imageUtils.getImageFromEpub("nonexistent");
        assertNull(result);
    }
}