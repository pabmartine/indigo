package com.martinia.indigo.book.domain.service.resource;

import org.springframework.core.io.Resource;

public interface ObtainBookByPathUseCase {

	Resource getEpub(String path);

}
