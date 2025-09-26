package com.martinia.indigo.shared.image;

import com.martinia.indigo.metadata.domain.ImageTagMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private ImageTagMetadataExtractor imageTagMetadataExtractor;

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageServiceImpl(imageTagMetadataExtractor);
    }

    @Test
    @DisplayName("Should return metadata when extraction is successful")
    void shouldReturnMetadataWhenExtractionIsSuccessful() throws IOException {
        // Given
        Path path = Paths.get("dummy/path");
        ImageTagMetadata expectedMetadata = ImageTagMetadata.builder()
                .format("JPEG")
                .width(100)
                .height(100)
                .tags(List.of("tag1", "tag2"))
                .build();
        when(imageTagMetadataExtractor.extract(path)).thenReturn(expectedMetadata);

        // When
        ImageTagMetadata actualMetadata = imageService.getImageTagMetadata(path);

        // Then
        assertEquals(expectedMetadata, actualMetadata);
    }

    @Test
    @DisplayName("Should throw IOException when extraction fails")
    void shouldThrowIOExceptionWhenExtractionFails() throws IOException {
        // Given
        Path path = Paths.get("dummy/path");
        when(imageTagMetadataExtractor.extract(path)).thenThrow(new IOException());

        // Then
        assertThrows(IOException.class, () -> {
            // When
            imageService.getImageTagMetadata(path);
        });
    }
}