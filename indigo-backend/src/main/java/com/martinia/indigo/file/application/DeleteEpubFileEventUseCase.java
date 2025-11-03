package com.martinia.indigo.file.application;

import com.martinia.indigo.file.domain.events.EpubFileDeletedEvent;

public interface DeleteEpubFileEventUseCase {

    void deleteEpubFile(EpubFileDeletedEvent event);

}
