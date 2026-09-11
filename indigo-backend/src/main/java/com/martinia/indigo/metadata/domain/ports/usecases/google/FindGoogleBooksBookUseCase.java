package com.martinia.indigo.metadata.domain.ports.usecases.google;

import java.util.List;

import com.martinia.indigo.metadata.domain.model.BookMetadataResult;

public interface FindGoogleBooksBookUseCase {

	BookMetadataResult findBook(String title, List<String> authors);

}
