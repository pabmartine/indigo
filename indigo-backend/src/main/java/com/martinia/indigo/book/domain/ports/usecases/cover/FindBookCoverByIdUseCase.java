package com.martinia.indigo.book.domain.ports.usecases.cover;

import java.util.Optional;

public interface FindBookCoverByIdUseCase {

	Optional<byte[]> getCover(String bookId);

}
