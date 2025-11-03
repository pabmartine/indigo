package com.martinia.indigo.file.application.events;

import com.martinia.indigo.file.application.DeleteEpubFileEventUseCase;
import com.martinia.indigo.file.domain.FileRepository;
import com.martinia.indigo.file.domain.events.EpubFileDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteEpubFileEventUseCaseImpl implements DeleteEpubFileEventUseCase {

    private final FileRepository fileRepository;

    @Override
    @EventListener
    public void deleteEpubFile(EpubFileDeletedEvent event) {
        log.debug("Deleting epub file with id {}", event.getFileId());
        fileRepository.deleteById(event.getFileId());
    }
}