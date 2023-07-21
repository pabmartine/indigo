package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.model.Search;
import com.martinia.indigo.common.util.UtilComponent;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsAuthorPort;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorPort;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindAuthorMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FindAuthorMetadataUseCaseImpl implements FindAuthorMetadataUseCase {

	@Resource
	protected AuthorRepository authorRepository;

	@Resource
	protected BookRepository bookRepository;

	@Resource
	protected ConfigurationRepository configurationRepository;

	@Resource
	private Optional<FindGoodReadsAuthorPort> findGoodReadsAuthorPort;

	@Resource
	private Optional<FindWikipediaAuthorPort> findWikipediaAuthorPort;

	@Resource
	private UtilComponent utilComponent;

	@Override
	public void find(final String authorId, final boolean override, final long lastExecution, final String lang) {

		authorRepository.findById(authorId).ifPresent(author -> {

			if (override || refreshAuthorMetadata(author)) {

				final Long pullTime = configurationRepository.findByKey("metadata.pull")
						.map(configuration -> Long.parseLong(configuration.getValue()))
						.orElse(null);

				final String goodreads = configurationRepository.findByKey("goodreads.key")
						.map(ConfigurationMongoEntity::getValue)
						.orElse(null);

				author.setDescription(null);
				author.setImage(null);
				author.setProvider(null);

				String[] wikipedia = findWikipediaAuthorPort.map(wiki -> wiki.findAuthor(author.getName(), lang, 0)).orElse(null);

				if (wikipedia == null && !lang.equals("en")) {
					wikipedia = findWikipediaAuthorPort.map(wiki -> wiki.findAuthor(author.getName(), "en", 0)).orElse(null);
				}

				if (wikipedia == null || wikipedia[1] == null) {
					String[] goodReads = findGoodReadsAuthorPort.map(gr -> gr.findAuthor(goodreads, author.getName())).orElse(null);
					if (goodReads != null) {
						author.setDescription(goodReads[0]);
						author.setImage(goodReads[1]);
						author.setProvider(goodReads[2]);

						long milliseconds = (System.currentTimeMillis() - lastExecution);

						if (milliseconds < pullTime) {
							try {
								Thread.sleep(pullTime);
							}
							catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						}
					}

				}

				if (wikipedia != null) {
					if (StringUtils.isEmpty(author.getDescription()) && !StringUtils.isEmpty(wikipedia[0])) {
						author.setDescription(wikipedia[0]);
					}
					if (StringUtils.isEmpty(author.getImage()) && !StringUtils.isEmpty(wikipedia[1])) {
						author.setImage(wikipedia[1]);
					}
					if (StringUtils.isEmpty(author.getProvider()) && !StringUtils.isEmpty(wikipedia[2])) {
						author.setProvider(wikipedia[2]);
					}
				}

				if (!StringUtils.isEmpty(author.getImage())) {
					author.setImage(utilComponent.getBase64Url(author.getImage()));
				}

				if (StringUtils.isEmpty(author.getImage())) {
					Search search = new Search();
					search.setAuthor(author.getSort());
					List<BookMongoEntity> books = bookRepository.findAll(search, 0, Integer.MAX_VALUE, "pubDate", "desc");
					for (BookMongoEntity book : books) {
						String image = utilComponent.getImageFromEpub(book.getPath(), "autor", "author");
						author.setImage(image);
						if (author.getImage() != null) {
							break;
						}
					}

				}

				author.setLastMetadataSync(Calendar.getInstance().getTime());
				authorRepository.save(author);

				log.info("Found metadata for {}", author.getName());

			}
		});
	}

	private boolean refreshAuthorMetadata(final AuthorMongoEntity author) {
		return (author == null || StringUtils.isEmpty(author.getDescription()) || StringUtils.isEmpty(author.getImage())
				|| StringUtils.isEmpty(author.getProvider())) && (author.getLastMetadataSync() == null || author.getLastMetadataSync()
				.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime()
				.plusDays(7)
				.isBefore(Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
	}
}
