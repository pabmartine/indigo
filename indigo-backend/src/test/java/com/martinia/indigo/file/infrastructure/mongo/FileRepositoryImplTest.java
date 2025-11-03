package com.martinia.indigo.file.infrastructure.mongo;

import com.martinia.indigo.file.domain.model.File;
import com.martinia.indigo.file.infrastructure.mongo.entities.FileMongoEntity;
import com.martinia.indigo.file.infrastructure.mongo.repositories.FileMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileRepositoryImplTest {

    @Mock
    private FileMongoRepository fileMongoRepository;

    @InjectMocks
    private FileRepositoryImpl fileRepository;

    private UUID testFileId;
    private Path testPath;
    private File testFile;
    private FileMongoEntity testEntity;

    @BeforeEach
    void setUp() {
        testFileId = UUID.randomUUID();
        testPath = Path.of("/test/path/file.txt");

        testFile = File.builder()
                .id(testFileId)
                .path(testPath)
                .build();

        testEntity = FileMongoEntity.builder()
                .id(testFileId)
                .path(testPath.toString())
                .build();
    }

    @Test
    void deleteById_ShouldCallRepositoryDeleteById() {
        // When
        fileRepository.deleteById(testFileId);

        // Then
        verify(fileMongoRepository).deleteById(testFileId);
    }

    @Test
    void findByPath_WhenFileExists_ShouldReturnFile() {
        // Given
        when(fileMongoRepository.findByPath(testPath.toString())).thenReturn(Optional.of(testEntity));

        // When
        Optional<File> result = fileRepository.findByPath(testPath);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testFileId);
        assertThat(result.get().getPath()).isEqualTo(testPath);
        verify(fileMongoRepository).findByPath(testPath.toString());
    }

    @Test
    void findByPath_WhenFileDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(fileMongoRepository.findByPath(testPath.toString())).thenReturn(Optional.empty());

        // When
        Optional<File> result = fileRepository.findByPath(testPath);

        // Then
        assertThat(result).isEmpty();
        verify(fileMongoRepository).findByPath(testPath.toString());
    }

    @Test
    void save_ShouldCallRepositorySaveWithConvertedEntity() {
        // When
        fileRepository.save(testFile);

        // Then
        verify(fileMongoRepository).save(any(FileMongoEntity.class));
    }

    @Test
    void save_ShouldConvertDomainToEntityCorrectly() {
        // Given
        FileMongoEntity expectedEntity = FileMongoEntity.builder()
                .id(testFileId)
                .path(testPath.toString())
                .build();

        // When
        fileRepository.save(testFile);

        // Then
        verify(fileMongoRepository).save(any(FileMongoEntity.class));
    }

    @Test
    void findByPath_WithDifferentPaths_ShouldHandleCorrectly() {
        // Given
        Path windowsPath = Path.of("C:\\test\\path\\file.txt");
        Path unixPath = Path.of("/usr/local/bin/test");

        FileMongoEntity windowsEntity = FileMongoEntity.builder()
                .id(UUID.randomUUID())
                .path(windowsPath.toString())
                .build();

        when(fileMongoRepository.findByPath(windowsPath.toString())).thenReturn(Optional.of(windowsEntity));
        when(fileMongoRepository.findByPath(unixPath.toString())).thenReturn(Optional.empty());

        // When
        Optional<File> windowsResult = fileRepository.findByPath(windowsPath);
        Optional<File> unixResult = fileRepository.findByPath(unixPath);

        // Then
        assertThat(windowsResult).isPresent();
        assertThat(windowsResult.get().getPath()).isEqualTo(windowsPath);
        assertThat(unixResult).isEmpty();

        verify(fileMongoRepository).findByPath(windowsPath.toString());
        verify(fileMongoRepository).findByPath(unixPath.toString());
    }

    @Test
    void toDomain_ShouldConvertEntityToDomainCorrectly() {
        // Given - Test the private method through public method
        when(fileMongoRepository.findByPath(testPath.toString())).thenReturn(Optional.of(testEntity));

        // When
        Optional<File> result = fileRepository.findByPath(testPath);

        // Then
        assertThat(result).isPresent();
        File convertedFile = result.get();
        assertThat(convertedFile.getId()).isEqualTo(testEntity.getId());
        assertThat(convertedFile.getPath().toString()).isEqualTo(testEntity.getPath());
    }

    @Test
    void save_WithNullValues_ShouldHandleGracefully() {
        // Given
        File fileWithNullId = File.builder()
                .id(null)
                .path(testPath)
                .build();

        // When
        fileRepository.save(fileWithNullId);

        // Then
        verify(fileMongoRepository).save(any(FileMongoEntity.class));
    }

    @Test
    void findByPath_WithComplexPaths_ShouldWork() {
        // Given
        Path complexPath = Path.of("/very/deep/nested/path/with/special-chars_123/file.extension");
        FileMongoEntity complexEntity = FileMongoEntity.builder()
                .id(UUID.randomUUID())
                .path(complexPath.toString())
                .build();

        when(fileMongoRepository.findByPath(complexPath.toString())).thenReturn(Optional.of(complexEntity));

        // When
        Optional<File> result = fileRepository.findByPath(complexPath);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPath()).isEqualTo(complexPath);
        verify(fileMongoRepository).findByPath(complexPath.toString());
    }
}