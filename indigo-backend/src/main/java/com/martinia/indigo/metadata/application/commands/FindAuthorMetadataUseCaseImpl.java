package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.common.util.ImageUtils;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryAuthorPort;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorPort;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindAuthorMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Optional;

@Slf4j
@Service
public class FindAuthorMetadataUseCaseImpl implements FindAuthorMetadataUseCase {

	@Resource
	protected AuthorRepository authorRepository;

	@Resource
	private Optional<FindWikipediaAuthorPort> findWikipediaAuthorPort;

	@Resource
	private Optional<FindOpenLibraryAuthorPort> findOpenLibraryAuthorPort;

	@Resource
	private ImageUtils imageUtils;

	@Override
	public MetadataItemResult find(final String authorId, final boolean override, final long lastExecution, final String lang) {

		return authorRepository.findById(authorId).map(author -> {

			if (!override && !refreshAuthorMetadata(author)) {
				return MetadataItemResult.SKIPPED;
			}

			boolean providerSucceeded = false;
			boolean translationFailed = false;
            boolean providerFailed = false;
			String[] wikipedia = null;

			if (findWikipediaAuthorPort.isPresent()) {
				try {
					wikipedia = findWikipediaAuthorPort.get().findAuthor(author.getName(), lang, 0);
					providerSucceeded = true;
				}
				catch (RuntimeException exception) {
                    providerFailed = true;
					log.warn("Wikipedia ({}) failed for {}: {}", lang, author.getName(), org.springframework.core.NestedExceptionUtils.getMostSpecificCause(exception).toString());
                    com.martinia.indigo.metadata.application.ProviderDiagnostics.record("WIKIPEDIA", "Obtener autor", exception);
				}
				if (wikipedia == null && !"en".equals(lang)) {
					try {
						wikipedia = findWikipediaAuthorPort.get().findAuthor(author.getName(), "en", 0);
						providerSucceeded = true;
					}
					catch (RuntimeException exception) {
                    providerFailed = true;
						log.warn("Wikipedia (en) failed for {}: {}", author.getName(), org.springframework.core.NestedExceptionUtils.getMostSpecificCause(exception).toString());
                    com.martinia.indigo.metadata.application.ProviderDiagnostics.record("WIKIPEDIA", "Obtener autor", exception);
					}
				}
			}

			String[] openLibrary = null;
			if ((wikipedia == null || StringUtils.isEmpty(wikipedia[0]) || StringUtils.isEmpty(wikipedia[1]))
					&& findOpenLibraryAuthorPort.isPresent()) {
				try {
					openLibrary = findOpenLibraryAuthorPort.get().findAuthor(author.getName());
					providerSucceeded = true;
				}
				catch (RuntimeException exception) {
                    providerFailed = true;
					log.warn("Open Library failed for {}: {}", author.getName(), org.springframework.core.NestedExceptionUtils.getMostSpecificCause(exception).toString());
                    com.martinia.indigo.metadata.application.ProviderDiagnostics.record("OPEN_LIBRARY", "Obtener autor", exception);
					translationFailed = true;
				}
			}

			boolean found = applyMetadata(author, wikipedia, override);
			found |= applyMetadata(author, openLibrary, override && wikipedia == null);
			if ((!found && (!providerSucceeded || providerFailed)) || (translationFailed && StringUtils.isBlank(author.getDescription()))) {
				return MetadataItemResult.ERROR;
			}

			if (!providerFailed) author.setLastMetadataSync(Calendar.getInstance().getTime());
			authorRepository.save(author);

			if (found) {
				log.info("Found metadata for {}", author.getName());
				return MetadataItemResult.FOUND;
			}
			log.info("No external metadata match found for {}", author.getName());
			return MetadataItemResult.NOT_FOUND;
		}).orElse(MetadataItemResult.SKIPPED);
	}

	private boolean applyMetadata(final AuthorMongoEntity author, final String[] metadata, final boolean override) {
		if (metadata == null || metadata.length < 3) {
			return false;
		}
		final boolean found = StringUtils.isNotEmpty(metadata[0]) || StringUtils.isNotEmpty(metadata[1]);
		if ((override || StringUtils.isEmpty(author.getDescription())) && StringUtils.isNotEmpty(metadata[0])) {
			author.setDescription(metadata[0]);
			recordSource(author, "description", metadata[2]);
		}
		if ((override || StringUtils.isEmpty(author.getImage())) && StringUtils.isNotEmpty(metadata[1])) {
			final String image = imageUtils.getBase64Url(metadata[1]);
			if (StringUtils.isNotEmpty(image)) {
				author.setImage(image);
				recordSource(author, "image", metadata[2]);
			}
		}
		if ((override || StringUtils.isEmpty(author.getProvider())) && StringUtils.isNotEmpty(metadata[2])) {
			author.setProvider(metadata[2]);
		}
		return found;
	}

	private void recordSource(AuthorMongoEntity author, String field, String provider) {
		var sources = new java.util.HashMap<String, String>(Optional.ofNullable(author.getMetadataSources()).orElseGet(java.util.Map::of));
		sources.put(field, provider);
		author.setMetadataSources(sources);
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
