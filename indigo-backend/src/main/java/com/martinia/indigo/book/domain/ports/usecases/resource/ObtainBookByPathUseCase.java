package com.martinia.indigo.book.domain.ports.usecases.resource;

import org.springframework.core.io.Resource;

public interface ObtainBookByPathUseCase {

	Resource getEpub(String path);

}
