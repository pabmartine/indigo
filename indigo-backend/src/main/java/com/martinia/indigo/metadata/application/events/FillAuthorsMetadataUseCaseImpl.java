package com.martinia.indigo.metadata.application.events;

import com.martinia.indigo.adapters.out.sqlite.service.CalibreRepository;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.common.model.NumBooks;
import com.martinia.indigo.metadata.domain.ports.usecases.events.FillAuthorsMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@Service
public class FillAuthorsMetadataUseCaseImpl implements FillAuthorsMetadataUseCase {

	@Resource
	private CalibreRepository calibreRepository;

	@Resource
	protected AuthorRepository authorRepository;

	@Resource
	protected BookRepository bookRepository;

	@Resource
	protected AuthorMongoMapper authorMongoMapper;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	@Transactional
	public void fillAuthors(final String bookId) {

		boolean updateBook = false;

		final Book book = calibreRepository.findBookById(bookId);

		// Authors
		if (!CollectionUtils.isEmpty(book.getAuthors())) {

			List<Author> authors = calibreRepository.findAuthorsByBook(book.getId());

			if (!CollectionUtils.isEmpty(authors)) {

				for (Author author : authors) {

					if (author.getName().equals("VV., AA.") || author.getSort().equals("VV., AA.")) {
						author.setName("AA. VV.");
						author.setSort("AA. VV.");
					}

					if (!book.getAuthors().contains(author.getSort())) {

						String[] tokens = author.getSort().replace(",", "").split(" ");
						boolean _contains = false;
						for (String a : book.getAuthors()) {
							boolean contains = true;
							for (String t : tokens) {
								if (!a.contains(t)) {
									contains = false;
									break;
								}
							}
							if (contains) {
								_contains = true;

								if (!a.equals(author.getSort())) {
									author.setSort(a);
									updateBook = true;
								}
							}
						}

						if (!_contains) {
							book.getAuthors().add(author.getSort());
							updateBook = true;
						}

					}

					Author domainAuthor = new Author();
					domainAuthor.setName(author.getName());
					domainAuthor.setSort(author.getSort());
					domainAuthor.setNumBooks(new NumBooks());
					for (String lang : book.getLanguages()) {
						domainAuthor.getNumBooks().getLanguages().put(lang, 1);
					}

					//save author
					AuthorMongoEntity authorMongoEntity = authorMongoMapper.domain2Entity(domainAuthor);
					//TODO: Consultar en mongodb por si ya existe
					authorRepository.findByName(author.getName()).stream().findFirst().ifPresent(entity -> {
						authorMongoEntity.setId(entity.getId());
						authorMongoEntity.getNumBooks().setTotal(entity.getNumBooks().getTotal() + 1);
						entity.getNumBooks().getLanguages().keySet().forEach(key -> {
							if (authorMongoEntity.getNumBooks().getLanguages().get(key) != null) {
								authorMongoEntity.getNumBooks()
										.getLanguages()
										.put(key, authorMongoEntity.getNumBooks().getLanguages().get(key) + entity.getNumBooks()
												.getLanguages()
												.get(key));
							}
							else {
								authorMongoEntity.getNumBooks().getLanguages().put(key, entity.getNumBooks().getLanguages().get(key));
							}
						});
					});
					authorRepository.save(authorMongoEntity);
				}

				if (updateBook == true) {
					String id = bookRepository.findByPath(book.getPath()).get().getId();
					book.setId(id);
					bookRepository.save(bookMongoMapper.domain2Entity(book));
				}
			}

			log.info("Authors '{}' for book '{}' saved to database", book.getAuthors(), book.getTitle());

		}

	}

}
