package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.ports.events.InitialLoadStartedEvent;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartInitialLoadCommandUseCase;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Slf4j
@Service
public class StartInitialLoadCommandUseCaseImpl implements StartInitialLoadCommandUseCase {

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Resource
	protected TagRepository tagRepository;

	@Resource
	protected AuthorRepository authorRepository;

	@Resource
	protected BookRepository bookRepository;

	@Resource
	protected EventBus eventBus;

	@Override
	@Transactional
	public void start() {

		metadataSingleton.setMessage("indexing_books");

		tagRepository.deleteAll();
		authorRepository.deleteAll();
		bookRepository.deleteAll();

		eventBus.publish(InitialLoadStartedEvent.builder().build());

	}

}
