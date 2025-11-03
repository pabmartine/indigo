package com.martinia.indigo.file.application.events;

import com.martinia.indigo.file.domain.FileRepository;
import com.martinia.indigo.file.domain.events.EpubFileDeletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DeleteEpubFileEventUseCaseImplUnitTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private DeleteEpubFileEventUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        // No specific setup needed for this test class beyond mock injection
    }

    @Test
    void deleteEpubFile_ShouldCallFileRepositoryDeleteById() {
        // Given
        UUID fileId = UUID.randomUUID();
        EpubFileDeletedEvent event = new EpubFileDeletedEvent(fileId);

        // When
        useCase.deleteEpubFile(event);

        // Then
        verify(fileRepository).deleteById(fileId);
    }
}
