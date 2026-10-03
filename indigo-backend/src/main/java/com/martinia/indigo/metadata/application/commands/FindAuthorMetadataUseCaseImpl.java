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

			Lookup lookup = new Lookup(author, override);
			findOpenLibraryAuthorCatalogPort.ifPresent(port -> lookup.obtain("OPEN_LIBRARY", "Catálogo local de autores",
					() -> port.findAuthor(author.getName())));

			java.util.Set<String> languages = new java.util.LinkedHashSet<>();
			languages.add("es");
			String requestedLanguage = com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation.normalizeLanguage(lang);
			if (requestedLanguage != null) languages.add(requestedLanguage);
			languages.add("en");
			findWikipediaAuthorPort.ifPresent(port -> {
				for (String language : languages) {
					if (!lookup.missing()) break;
					lookup.obtain("WIKIPEDIA", "Obtener autor (" + language + ")",
							() -> port.findAuthor(author.getName(), language, 0));
				}
			});
			if (lookup.missing()) findOpenLibraryAuthorPort.ifPresent(port -> lookup.obtain("OPEN_LIBRARY", "Obtener autor",
					() -> port.findAuthor(author.getName())));

			boolean incompleteFailure = lookup.failed && lookup.missing();
			if (!lookup.succeeded && !lookup.found) return MetadataItemResult.ERROR;
			if (!lookup.failed) author.setLastMetadataSync(Calendar.getInstance().getTime());
			if (lookup.found || !incompleteFailure) authorRepository.save(author);
			if (incompleteFailure) return MetadataItemResult.ERROR;
			log.info("Author metadata result for {}: found={} descriptionPresent={} imagePresent={}", author.getName(),
					lookup.found, StringUtils.isNotBlank(author.getDescription()), StringUtils.isNotBlank(author.getImage()));
			return lookup.found ? MetadataItemResult.FOUND : MetadataItemResult.NOT_FOUND;
		}).orElse(MetadataItemResult.SKIPPED);
	}

	private final class Lookup {
		private final AuthorMongoEntity author;
		private final boolean override;
		private boolean descriptionObtained;
		private boolean imageObtained;
		private boolean succeeded;
		private boolean failed;
		private boolean found;

		private Lookup(AuthorMongoEntity author, boolean override) {
			this.author = author;
			this.override = override;
		}

		private boolean needsDescription() {
			return override ? !descriptionObtained : StringUtils.isBlank(author.getDescription());
		}

		private boolean needsImage() {
			return override ? !imageObtained : StringUtils.isBlank(author.getImage());
		}

		private boolean missing() {
			return needsDescription() || needsImage();
		}

		private void obtain(String provider, String operation, java.util.function.Supplier<String[]> request) {
			String[] metadata;
			try {
				metadata = request.get();
				succeeded = true;
			}
			catch (RuntimeException exception) {
				com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
				failed = true;
				log.warn("{} failed for {}: {}", operation, author.getName(), exception.toString());
				com.martinia.indigo.metadata.application.ProviderDiagnostics.record(provider, operation, exception);
				metadata = exception instanceof com.martinia.indigo.metadata.application.AuthorMetadataTranslationException partial
						? partial.getPartialMetadata() : null;
			}
			if (metadata == null || metadata.length < 3) return;
			boolean changed = false;
			if (needsDescription() && StringUtils.isNotBlank(metadata[0])) {
				author.setDescription(metadata[0]);
				recordSource(author, "description", metadata[2]);
				descriptionObtained = true;
				changed = true;
			}
			if (needsImage() && StringUtils.isNotBlank(metadata[1])) {
				try {
					String image = imageUtils.getBase64AuthorUrl(metadata[1]);
					if (StringUtils.isNotBlank(image)) {
						author.setImage(image);
						recordSource(author, "image", metadata[2]);
						imageObtained = true;
						changed = true;
					}
				}
				catch (RuntimeException exception) {
					com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
					failed = true;
					com.martinia.indigo.metadata.application.ProviderDiagnostics.record(provider, "Descargar foto del autor", exception);
				}
			}
			if (changed && (StringUtils.isBlank(author.getProvider()) || override && !found)) author.setProvider(metadata[2]);
			found |= changed;
		}
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
