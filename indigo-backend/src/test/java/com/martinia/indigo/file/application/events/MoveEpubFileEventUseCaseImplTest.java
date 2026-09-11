package com.martinia.indigo.file.application.events;

import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.file.domain.FileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoveEpubFileEventUseCaseImplTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UploadEpubFilesSingleton uploadEpubFilesSingleton;

    @InjectMocks
    private MoveEpubFileEventUseCaseImpl useCase;

    @TempDir
    Path tempDir;

    @Test
    void move_WhenEpubHasNoCover_ShouldStillMoveTheBook() throws IOException {
        Path sourceDirectory = Files.createDirectory(tempDir.resolve("source"));
        Path sourcePath = Files.writeString(sourceDirectory.resolve("book.epub"), "epub");
        Path targetPath = tempDir.resolve("library").resolve("Author").resolve("Book (en)");
        when(fileRepository.findByPath(sourcePath)).thenReturn(Optional.empty());

        useCase.move(sourcePath, targetPath);

        assertTrue(Files.exists(targetPath.resolve("book.epub")));
        verify(uploadEpubFilesSingleton).addMove();
        verify(uploadEpubFilesSingleton, never()).addMoveError();
    }

    @Test
    void move_WhenSourceDirectoriesAreEmpty_ShouldRemoveThemButPreserveUploadsRoot() throws IOException {
        Path uploadsRoot = Files.createDirectory(tempDir.resolve("uploads"));
        Path sourceDirectory = Files.createDirectories(uploadsRoot.resolve("batch").resolve("book"));
        Path sourcePath = Files.writeString(sourceDirectory.resolve("book.epub"), "epub");
        Path targetPath = tempDir.resolve("library").resolve("Author").resolve("Book (en)");
        ReflectionTestUtils.setField(useCase, "uploadsPath", uploadsRoot.toString());
        when(fileRepository.findByPath(sourcePath)).thenReturn(Optional.empty());

        useCase.move(sourcePath, targetPath);

        assertFalse(Files.exists(sourceDirectory));
        assertFalse(Files.exists(uploadsRoot.resolve("batch")));
        assertTrue(Files.isDirectory(uploadsRoot));
        assertTrue(Files.exists(targetPath.resolve("book.epub")));
    }

    @Test
    void move_WhenSourceDirectoryContainsAnotherFile_ShouldKeepIt() throws IOException {
        Path uploadsRoot = Files.createDirectory(tempDir.resolve("uploads"));
        Path sourceDirectory = Files.createDirectory(uploadsRoot.resolve("batch"));
        Path sourcePath = Files.writeString(sourceDirectory.resolve("book.epub"), "epub");
        Path pendingFile = Files.writeString(sourceDirectory.resolve("pending.txt"), "pending");
        Path targetPath = tempDir.resolve("library").resolve("Author").resolve("Book (en)");
        ReflectionTestUtils.setField(useCase, "uploadsPath", uploadsRoot.toString());
        when(fileRepository.findByPath(sourcePath)).thenReturn(Optional.empty());

        useCase.move(sourcePath, targetPath);

        assertTrue(Files.exists(pendingFile));
        assertTrue(Files.isDirectory(sourceDirectory));
        assertTrue(Files.isDirectory(uploadsRoot));
    }

    @Test
    void move_WhenBookExistsAndTargetCoverIsMissing_ShouldRestoreCover() throws IOException {
        Path uploadsRoot = Files.createDirectory(tempDir.resolve("uploads"));
        Path sourceDirectory = Files.createDirectory(uploadsRoot.resolve("batch"));
        Path sourcePath = Files.writeString(sourceDirectory.resolve("book.epub"), "original");
        Files.writeString(sourceDirectory.resolve("cover.jpg"), "cover");
        Path targetPath = Files.createDirectories(tempDir.resolve("library").resolve("Author").resolve("Book (en)"));
        Files.writeString(targetPath.resolve("book.epub"), "original");
        ReflectionTestUtils.setField(useCase, "uploadsPath", uploadsRoot.toString());
        when(fileRepository.findByPath(sourcePath)).thenReturn(Optional.empty());

        useCase.move(sourcePath, targetPath);

        assertFalse(Files.exists(sourceDirectory));
        assertTrue(Files.exists(targetPath.resolve("cover.jpg")));
    }

    @Test
    void conflictingContentIsPreserved() throws IOException {
        Path source = Files.writeString(tempDir.resolve("book.epub"), "new content");
        Path target = Files.createDirectory(tempDir.resolve("library"));
        Path existing = Files.writeString(target.resolve("book.epub"), "old content");
        useCase.move(source, target);
        org.junit.jupiter.api.Assertions.assertEquals("new content", Files.readString(source));
        org.junit.jupiter.api.Assertions.assertEquals("old content", Files.readString(existing));
        verify(uploadEpubFilesSingleton).addMoveError();
        verify(uploadEpubFilesSingleton, never()).addMove();
    }
}
