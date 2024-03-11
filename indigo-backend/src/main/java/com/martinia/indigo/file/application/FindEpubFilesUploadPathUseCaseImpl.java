package com.martinia.indigo.file.application;

import com.martinia.indigo.file.domain.ports.usecases.FindEpubFilesUploadPathUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class FindEpubFilesUploadPathUseCaseImpl implements FindEpubFilesUploadPathUseCase {

	@Value("${book.library.uploads}")
	private String uploadsPath;

	@Override
	public String findPath() {
		return uploadsPath;
	}

}
