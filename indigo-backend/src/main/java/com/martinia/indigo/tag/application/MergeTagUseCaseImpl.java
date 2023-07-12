package com.martinia.indigo.tag.application;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.MergeTagUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class MergeTagUseCaseImpl implements MergeTagUseCase {

	@Resource
	private TagRepository tagRepository;

	@Resource
	private BookRepository bookRepository;

	@Override
	public void merge(final String source, final String target) {
		tagRepository.findById(source).ifPresent(entitySource -> {
			tagRepository.findById(target).ifPresent(entityTarget -> {

				List<BookMongoEntity> books = bookRepository.findByTag(entitySource.getName());
				books.forEach(book -> {
					book.getTags().replaceAll(b -> b.equals(entitySource.getName()) ? entityTarget.getName() : b);
					book.setTags(new ArrayList<>(new LinkedHashSet<>(book.getTags())));
				});
				bookRepository.saveAll(books);

				entityTarget.getNumBooks().setTotal(entityTarget.getNumBooks().getTotal() + entitySource.getNumBooks().getTotal());
				entitySource.getNumBooks().getLanguages().keySet().forEach(lang -> {
					if (entityTarget.getNumBooks().getLanguages().get(lang) != null) {
						entityTarget.getNumBooks().getLanguages().put(lang,
								entityTarget.getNumBooks().getLanguages().get(lang) + entitySource.getNumBooks().getLanguages().get(lang));
					}
					else {
						entityTarget.getNumBooks().getLanguages().put(lang, entitySource.getNumBooks().getLanguages().get(lang));
					}
				});
				tagRepository.save(entityTarget);
				tagRepository.delete(entitySource);
			});
		});
	}

}
