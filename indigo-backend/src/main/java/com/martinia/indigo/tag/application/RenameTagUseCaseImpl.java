package com.martinia.indigo.tag.application;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.RenameTagUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class RenameTagUseCaseImpl implements RenameTagUseCase {

	@Resource
	private TagRepository tagRepository;

	@Resource
	private BookRepository bookRepository;

	@Override
	public void rename(final String source, final String target) {
		tagRepository.findById(source).ifPresent(tag -> {
			List<BookMongoEntity> books = bookRepository.findByTag(tag.getName());
			books.forEach(book -> book.getTags().replaceAll(b -> b.equals(tag.getName()) ? target : b));
			bookRepository.saveAll(books);

			tag.setName(target);
			tagRepository.save(tag);
		});
	}

}
