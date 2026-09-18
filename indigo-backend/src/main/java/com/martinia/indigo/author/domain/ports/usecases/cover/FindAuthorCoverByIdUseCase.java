package com.martinia.indigo.author.domain.ports.usecases.cover;

import java.util.Optional;

public interface FindAuthorCoverByIdUseCase {

	Optional<byte[]> getCover(String authorId);

}
