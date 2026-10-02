package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.common.util.ImageUtils;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryAuthorPort;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryAuthorCatalogPort;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorPort;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindAuthorMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Calendar;
import java.util.Optional;

@Slf4j
@Service
public class FindAuthorMetadataUseCaseImpl implements FindAuthorMetadataUseCase {

	@Resource
	protected AuthorRepository authorRepository;

	@Resource
	private Optional<FindOpenLibraryAuthorPort> findOpenLibraryAuthorPort;

	@Resource
	private Optional<FindOpenLibraryAuthorCatalogPort> findOpenLibraryAuthorCatalogPort;

	@Resource
	private Optional<FindWikipediaAuthorPort> findWikipediaAuthorPort;

	@Resource
	private ImageUtils imageUtils;

	@Override
	public MetadataItemResult find(final String authorId, final boolean override, final long lastExecution, final String lang) {

		return authorRepository.findById(authorId).map(author -> {

			if (!override && !refreshAuthorMetadata(author)) {
				log.info("Skipping author {}: metadata complete (descriptionPresent=true imagePresent=true)", author.getName());
				return MetadataItemResult.SKIPPED;
			}
			log.info("Finding author metadata for {}: descriptionPresent={} imagePresent={} override={}",
					author.getName(), StringUtils.isNotBlank(author.getDescription()), StringUtils.isNotBlank(author.getImage()), override);

			boolean providerSucceeded = false;
			boolean providerFailed = false;
			String[] catalog = null;
			if (findOpenLibraryAuthorCatalogPort.isPresent()) {
				try {
					catalog = findOpenLibraryAuthorCatalogPort.get().findAuthor(author.getName());
					providerSucceeded = true;
				}
				catch (RuntimeException exception) {
					providerFailed = true;
					log.warn("Open Library local author catalog failed for {}: {}", author.getName(), exception.toString());
					com.martinia.indigo.metadata.application.ProviderDiagnostics.record("OPEN_LIBRARY", "Catálogo local de autores", exception);
				}
			}

			String[] wikipedia = null;
			String[] wikipediaEnglish = null;
			if (missingMetadata(catalog) && findWikipediaAuthorPort.isPresent()) {
				try {
					wikipedia = findWikipediaAuthorPort.get().findAuthor(author.getName(), lang, 0);
					providerSucceeded = true;
				}
				catch (RuntimeException exception) {
					providerFailed = true;
					log.warn("Wikipedia ({}) failed for {}: {}", lang, author.getName(), exception.toString());
					com.martinia.indigo.metadata.application.ProviderDiagnostics.record("WIKIPEDIA", "Obtener autor", exception);
				}
				if (missingMetadata(catalog, wikipedia) && !"en".equals(lang)) {
					try {
						wikipediaEnglish = findWikipediaAuthorPort.get().findAuthor(author.getName(), "en", 0);
						providerSucceeded = true;
					}
					catch (RuntimeException exception) {
						providerFailed = true;
						log.warn("Wikipedia (en) failed for {}: {}", author.getName(), exception.toString());
						com.martinia.indigo.metadata.application.ProviderDiagnostics.record("WIKIPEDIA", "Obtener autor", exception);
					}
				}
			}

			String[] openLibrary = null;
			if (missingMetadata(catalog, wikipedia, wikipediaEnglish)
					&& findOpenLibraryAuthorPort.isPresent()) {
				try {
					openLibrary = findOpenLibraryAuthorPort.get().findAuthor(author.getName());
					providerSucceeded = true;
				}
				catch (RuntimeException exception) {
					providerFailed = true;
					log.warn("Open Library failed for {}: {}", author.getName(), exception.toString());
					com.martinia.indigo.metadata.application.ProviderDiagnostics.record("OPEN_LIBRARY", "Obtener autor", exception);
				}
			}

			boolean found = applyMetadata(author, catalog, override);
			found |= applyMetadata(author, wikipedia, override, catalog);
			found |= applyMetadata(author, wikipediaEnglish, override, catalog, wikipedia);
			found |= applyMetadata(author, openLibrary, override, catalog, wikipedia, wikipediaEnglish);
			if (!found && (!providerSucceeded || providerFailed)) return MetadataItemResult.ERROR;
			if (!providerFailed) author.setLastMetadataSync(Calendar.getInstance().getTime());
			authorRepository.save(author);

			if (found) {
				log.info("Found metadata for {}: descriptionPresent={} imagePresent={}", author.getName(),
						StringUtils.isNotBlank(author.getDescription()), StringUtils.isNotBlank(author.getImage()));
				return MetadataItemResult.FOUND;
			}
			log.info("No metadata obtained for missing fields of {}: descriptionPresent={} imagePresent={}",
					author.getName(), StringUtils.isNotBlank(author.getDescription()), StringUtils.isNotBlank(author.getImage()));
			return MetadataItemResult.NOT_FOUND;
		}).orElse(MetadataItemResult.SKIPPED);
	}

	private boolean missingMetadata(final String[]... candidates) {
		return !hasMetadataField(0, candidates) || !hasMetadataField(1, candidates);
	}

	private boolean hasMetadataField(final int field, final String[]... candidates) {
		for (String[] candidate : candidates) {
			if (candidate != null && candidate.length >= 3 && StringUtils.isNotBlank(candidate[field])) return true;
		}
		return false;
	}

	private boolean applyMetadata(final AuthorMongoEntity author, final String[] metadata, final boolean override,
			final String[]... preferred) {
		if (metadata == null || metadata.length < 3) {
			return false;
		}
		boolean found = false;
		if (((override && !hasMetadataField(0, preferred)) || StringUtils.isBlank(author.getDescription()))
				&& StringUtils.isNotBlank(metadata[0])) {
			author.setDescription(metadata[0]);
			recordSource(author, "description", metadata[2]);
			found = true;
		}
		if (((override && !hasMetadataField(1, preferred)) || StringUtils.isBlank(author.getImage()))
				&& StringUtils.isNotBlank(metadata[1])) {
			final String image = imageUtils.getBase64Url(metadata[1]);
			if (StringUtils.isNotEmpty(image)) {
				author.setImage(image);
				recordSource(author, "image", metadata[2]);
				found = true;
			}
		}
		if (((override && missingMetadata(preferred)) || StringUtils.isBlank(author.getProvider())) && StringUtils.isNotBlank(metadata[2])) {
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
		return author == null || StringUtils.isBlank(author.getDescription()) || StringUtils.isBlank(author.getImage());
	}
}
