package com.martinia.indigo.book.domain.service.cover;

import java.util.Optional;

public interface FindBookCoverByPathUseCase {

	Optional<String> getImage(String path);

}
