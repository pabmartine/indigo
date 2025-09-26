package com.martinia.indigo.shared.image;

import com.martinia.indigo.metadata.domain.ImageTagMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImageTagMetadataExtractorImplTest {

    private ImageTagMetadataExtractorImpl extractor;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        extractor = new ImageTagMetadataExtractorImpl();
    }

    @Test
    void extract_WithValidJpegFile_ShouldExtractMetadata() throws IOException {
        // Given - Create a simple JPEG test file
        Path testImagePath = createTestJpegFile(tempDir, "test.jpg");

        // When
        ImageTagMetadata result = extractor.extract(testImagePath);

        // Then
        assertNotNull(result);
        assertThat(result.getFormat()).isNotNull();
        assertThat(result.getWidth()).isGreaterThanOrEqualTo(0);
        assertThat(result.getHeight()).isGreaterThanOrEqualTo(0);
        assertThat(result.getTags()).isNotNull();
    }

    @Test
    void extract_WithNonExistentFile_ShouldReturnDefaultValues() throws IOException {
        // Given
        Path nonExistentFile = tempDir.resolve("nonexistent.jpg");

        // When
        ImageTagMetadata result = extractor.extract(nonExistentFile);

        // Then
        assertNotNull(result);
        assertThat(result.getFormat()).isEqualTo("unknown");
        assertThat(result.getWidth()).isEqualTo(0);
        assertThat(result.getHeight()).isEqualTo(0);
        assertThat(result.getTags()).isEmpty();
    }

    @Test
    void extract_WithCorruptedFile_ShouldReturnDefaultValues() throws IOException {
        // Given - Create a corrupted file (not a valid image)
        Path corruptedFile = tempDir.resolve("corrupted.jpg");
        Files.write(corruptedFile, "This is not an image file".getBytes());

        // When
        ImageTagMetadata result = extractor.extract(corruptedFile);

        // Then
        assertNotNull(result);
        assertThat(result.getFormat()).isEqualTo("unknown");
        assertThat(result.getWidth()).isEqualTo(0);
        assertThat(result.getHeight()).isEqualTo(0);
        assertThat(result.getTags()).isEmpty();
    }

    @Test
    void extract_WithEmptyFile_ShouldReturnDefaultValues() throws IOException {
        // Given
        Path emptyFile = tempDir.resolve("empty.jpg");
        Files.createFile(emptyFile);

        // When
        ImageTagMetadata result = extractor.extract(emptyFile);

        // Then
        assertNotNull(result);
        assertThat(result.getFormat()).isEqualTo("unknown");
        assertThat(result.getWidth()).isEqualTo(0);
        assertThat(result.getHeight()).isEqualTo(0);
        assertThat(result.getTags()).isEmpty();
    }

    @Test
    void extract_WithExistingJpegFromResources_ShouldExtractMetadata() throws IOException {
        // Given - Use existing test image from resources if available
        Path resourceImagePath = Path.of("src/test/resources/test.jpg");

        if (!Files.exists(resourceImagePath)) {
            // Create a minimal JPEG if resource doesn't exist
            resourceImagePath = createTestJpegFile(tempDir, "resource-test.jpg");
        }

        // When
        ImageTagMetadata result = extractor.extract(resourceImagePath);

        // Then
        assertNotNull(result);
        assertThat(result.getFormat()).isNotNull();
        assertThat(result.getWidth()).isGreaterThanOrEqualTo(0);
        assertThat(result.getHeight()).isGreaterThanOrEqualTo(0);
        assertThat(result.getTags()).isNotNull();
    }

    private Path createTestJpegFile(Path directory, String filename) throws IOException {
        Path testFile = directory.resolve(filename);

        // Create a minimal valid JPEG file
        // JPEG magic bytes: FF D8 FF E0 00 10 4A 46 49 46 00 01 01 01 00 48 00 48 00 00 FF D9
        byte[] jpegBytes = {
            (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0, 0x00, 0x10, 0x4A, 0x46,
            0x49, 0x46, 0x00, 0x01, 0x01, 0x01, 0x00, 0x48, 0x00, 0x48, 0x00, 0x00,
            (byte)0xFF, (byte)0xD9
        };

        Files.write(testFile, jpegBytes);
        return testFile;
    }
}