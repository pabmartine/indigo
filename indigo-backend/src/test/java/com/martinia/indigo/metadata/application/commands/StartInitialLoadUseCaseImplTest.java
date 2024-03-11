package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.events.InitialLoadStartedEvent;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class StartInitialLoadUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private StartInitialLoadUseCaseImpl startInitialLoadUseCase;

	@MockBean
	private MetadataSingleton metadataSingleton;

	@MockBean
	private TagRepository tagRepository;

	@MockBean
	private AuthorRepository authorRepository;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private EventBus eventBus;

	@Test
	public void testStart_OverrideTrue_ShouldDeleteDatabaseAndPublishEvent() {
		// Given
		boolean override = true;

		// When
		startInitialLoadUseCase.start(override);

		// Then
		// Verificar que los métodos deleteAll() de los repositorios son llamados una vez
		verify(tagRepository, times(1)).deleteAll();
		verify(authorRepository, times(1)).deleteAll();
		verify(bookRepository, times(1)).deleteAll();

		// Verificar que el método publish() del eventBus es llamado una vez con el evento correcto
		verify(eventBus, times(1)).publish(argThat(event -> event instanceof InitialLoadStartedEvent && ((InitialLoadStartedEvent) event).isOverride() == override));
	}

	@Test
	public void testStart_OverrideFalse_ShouldNotDeleteDatabaseAndPublishEvent() {
		// Given
		boolean override = false;

		// When
		startInitialLoadUseCase.start(override);

		// Then
		// Verificar que los métodos deleteAll() de los repositorios NO son llamados
		verify(tagRepository, never()).deleteAll();
		verify(authorRepository, never()).deleteAll();
		verify(bookRepository, never()).deleteAll();

		// Verificar que el método publish() del eventBus es llamado una vez con el evento correcto
		verify(eventBus, times(1)).publish(argThat(event -> event instanceof InitialLoadStartedEvent && ((InitialLoadStartedEvent) event).isOverride() == override));
	}

	// Otras pruebas para otros casos

}
