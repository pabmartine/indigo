package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StartFillAuthorsMetadataUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private MetadataSingleton metadataSingleton;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private AuthorRepository authorRepository;

	@MockBean
	private CommandBus commandBus;

	@Resource
	private StartFillAuthorsMetadataUseCaseImpl startFillAuthorsMetadataUseCase;

	@Test
	public void testStart_OverrideTrue_ShouldFindAndExecuteAuthorsMetadata() {
		// Given
		boolean override = true;
		String lang = "en";
		int batchSize = 100;
		List<String> languages = new ArrayList<>();
		languages.add("en");
		languages.add("es");
		Long numAuthors = 200L;
		when(bookRepository.getBookLanguages()).thenReturn(languages);
		when(authorRepository.count(languages)).thenReturn(numAuthors);
		when(metadataSingleton.getTotal()).thenReturn(0L);
		when(metadataSingleton.getCurrent()).thenReturn(0L);
		when(metadataSingleton.isRunning()).thenReturn(true);
		List<AuthorMongoEntity> authors = new ArrayList<>();
		for (long i = 1; i <= numAuthors; i++) {
			AuthorMongoEntity author = new AuthorMongoEntity();
			author.setId("author_id_" + i);
			authors.add(author);
		}
		when(authorRepository.findAll(anyList(), any(Pageable.class))).thenReturn(authors);
		when(metadataSingleton.isRunning()).thenReturn(true);
		when(metadataSingleton.isRunning()).thenReturn(false);

		// When
		startFillAuthorsMetadataUseCase.start(override, lang);

		// Then
		// Verificar que se llama al método getBookLanguages() del bookRepository
		verify(bookRepository, times(1)).getBookLanguages();
		// Verificar que el método count() del authorRepository es llamado una vez con la lista de idiomas como argumento
		verify(authorRepository, times(1)).count(languages);
		// Verificar que el método getTotal() del metadataSingleton es llamado una vez
		verify(metadataSingleton, times(1)).getTotal();

		// Verificar que el método setTotal() del metadataSingleton es llamado una vez con el valor actualizado
		verify(metadataSingleton, times(1)).setTotal(metadataSingleton.getTotal() + numAuthors);

		// Verificar que el método isRunning() del metadataSingleton es llamado el número correcto de veces
		verify(metadataSingleton, times(1)).isRunning();
	}

	// Otras pruebas para otros casos

}

