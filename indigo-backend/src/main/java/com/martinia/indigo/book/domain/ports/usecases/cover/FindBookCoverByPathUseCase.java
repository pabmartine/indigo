package com.martinia.indigo.book.domain.ports.usecases.cover;

import java.util.Optional;

public interface FindBookCoverByPathUseCase {

	Optional<String> getImage(String path);

}
