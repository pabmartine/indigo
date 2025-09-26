package com.martinia.indigo.file.domain.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class EpubFileDeletedEvent {

    private final UUID fileId;

}
