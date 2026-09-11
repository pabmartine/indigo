package com.martinia.indigo.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
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
    void getEpub_WhenPathEscapesLibrary_ShouldReturnNull() {
        Resource result = imageUtils.getEpub("../");
        assertNull(result);
    }

    @Test
    void getImageFromEpub_WhenFileDoesNotExist_ShouldReturnNull() {
        String result = imageUtils.getImageFromEpub("nonexistent");
        assertNull(result);
    }

	@Test
	void saveCoverAndGetThumbnail_NormalizesPngToJpegAndCreatesThumbnail() throws IOException {
		BufferedImage png = new BufferedImage(100, 300, BufferedImage.TYPE_INT_ARGB);
		png.setRGB(10, 10, 0x80FF0000);
		ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(png, "png", imageBytes);
		Path coverPath = tempDir.resolve("imported").resolve("cover.jpg");

		String thumbnail = imageUtils.saveCoverAndGetThumbnail(imageBytes.toByteArray(), coverPath);

		assertNotNull(thumbnail);
		assertNotNull(javax.imageio.ImageIO.read(coverPath.toFile()));
		byte[] thumbnailBytes = java.util.Base64.getDecoder().decode(thumbnail);
		BufferedImage thumbnailImage = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(thumbnailBytes));
		assertEquals(83, thumbnailImage.getWidth());
        assertEquals(249, thumbnailImage.getHeight());
	}
}
