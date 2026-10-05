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

	@Resource
	private com.martinia.indigo.common.singletons.MetadataSingleton metadataSingleton;

	@Override
	public MetadataItemResult find(final String authorId, final boolean override, final long lastExecution, final String lang) {
		return find(authorId, override, lastExecution, lang, 0);
	}

	@Override
	public MetadataItemResult find(String authorId, boolean override, long lastExecution, String lang, long runId) {
		boolean managed = runId > 0;
		java.util.function.BooleanSupplier active = () -> !Thread.currentThread().isInterrupted()
				&& (!managed || metadataSingleton.isActive(runId));
		ensureActive(active);

		return authorRepository.findById(authorId).map(author -> {

			if (!override && !refreshAuthorMetadata(author)) {
				com.martinia.indigo.metadata.application.ProviderDiagnostics.explain("El autor ya tenía descripción y foto; no necesita completar metadatos");
				log.info("Skipping author {}: metadata complete (descriptionPresent=true imagePresent=true)", author.getName());
				return MetadataItemResult.SKIPPED;
			}
			log.info("Finding author metadata for {}: descriptionPresent={} imagePresent={} override={}",
					author.getName(), StringUtils.isNotBlank(author.getDescription()), StringUtils.isNotBlank(author.getImage()), override);

			Lookup lookup = new Lookup(author, override, active);
			findOpenLibraryAuthorCatalogPort.ifPresent(port -> lookup.obtain("OPEN_LIBRARY", "Catálogo local de autores",
					() -> lookup.needsDescription() ? port.findAuthor(author.getName()) : port.findAuthor(author.getName(), false)));

			java.util.Set<String> languages = new java.util.LinkedHashSet<>();
			languages.add("es");
			String requestedLanguage = com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation.normalizeLanguage(lang);
			if (requestedLanguage != null) languages.add(requestedLanguage);
			languages.add("en");
			findWikipediaAuthorPort.ifPresent(port -> {
				for (String language : languages) {
					if (!lookup.missing()) break;
					lookup.obtain("WIKIPEDIA", "Obtener autor (" + language + ")",
							() -> com.martinia.indigo.common.util.WikipediaHttpRequests.whileActive(active,
									until -> { if (managed) metadataSingleton.setProviderWait(runId, "Wikipedia · " + author.getName(),
											until == null ? null : until.toEpochMilli()); },
									() -> lookup.needsDescription() ? port.findAuthor(author.getName(), language, 0)
											: port.findAuthor(author.getName(), language, 0, false)));
				}
			});
			// An active author dump is the Open Library source, including negative matches.
			// A readiness failure must not silently enable remote queries.
			boolean catalogAvailable = true;
			if (lookup.missing()) {
				try {
					catalogAvailable = findOpenLibraryAuthorCatalogPort.map(FindOpenLibraryAuthorCatalogPort::isAvailable).orElse(false);
				} catch (RuntimeException exception) {
					com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
					lookup.failed = true;
					log.warn("Could not determine author catalog availability", exception);
					com.martinia.indigo.metadata.application.ProviderDiagnostics.record("OPEN_LIBRARY", "Estado del catálogo local", exception);
				}
			}
			if (lookup.missing() && !catalogAvailable) findOpenLibraryAuthorPort.ifPresent(port -> lookup.obtain("OPEN_LIBRARY", "Obtener autor",
					() -> port.findAuthor(author.getName())));

			ensureActive(active);
			boolean incompleteFailure = lookup.failed && lookup.missing();
			if (!lookup.succeeded && !lookup.found) return MetadataItemResult.ERROR;
			if (!lookup.failed) author.setLastMetadataSync(Calendar.getInstance().getTime());
			if (lookup.found || !incompleteFailure) authorRepository.save(author);
			if (incompleteFailure) return MetadataItemResult.ERROR;
			if (!lookup.found && lookup.missing()) {
				String missingFields = lookup.needsDescription() && lookup.needsImage() ? "descripción ni foto"
						: lookup.needsDescription() ? "descripción" : "foto";
				com.martinia.indigo.metadata.application.ProviderDiagnostics.explain(
						"No se ha obtenido " + missingFields + " para los campos pendientes del autor; se conservan los datos que ya tenía");
			}
			log.info("Author metadata result for {}: found={} descriptionPresent={} imagePresent={}", author.getName(),
					lookup.found, StringUtils.isNotBlank(author.getDescription()), StringUtils.isNotBlank(author.getImage()));
			return lookup.found ? MetadataItemResult.FOUND : MetadataItemResult.NOT_FOUND;
		}).orElse(MetadataItemResult.SKIPPED);
	}

	private void ensureActive(java.util.function.BooleanSupplier active) {
		if (!active.getAsBoolean()) throw new java.util.concurrent.CancellationException("Author metadata cancelled");
	}

	private final class Lookup {
		private final AuthorMongoEntity author;
		private final boolean override;
		private boolean descriptionObtained;
		private boolean imageObtained;
		private boolean succeeded;
		private boolean failed;
		private boolean found;
		private final java.util.function.BooleanSupplier active;

		private Lookup(AuthorMongoEntity author, boolean override, java.util.function.BooleanSupplier active) {
			this.author = author;
			this.override = override;
			this.active = active;
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
			ensureActive(active);
			String[] metadata;
			boolean requestFailed = false;
			try {
				metadata = request.get();
				ensureActive(active);
				succeeded = true;
			}
			catch (RuntimeException exception) {
				com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
				failed = true;
				requestFailed = true;
				log.warn("Metadata provider {} operation {} failed for author {} ({})",
						provider, operation, author.getId(), author.getName(), exception);
				com.martinia.indigo.metadata.application.ProviderDiagnostics.record(provider, operation, exception);
				metadata = exception instanceof com.martinia.indigo.metadata.application.AuthorMetadataTranslationException partial
						? partial.getPartialMetadata() : null;
			}
			if (metadata == null || metadata.length < 3) {
				if (!requestFailed) com.martinia.indigo.metadata.application.ProviderDiagnostics.event(provider, operation, "NOT_FOUND", "No se ha obtenido una coincidencia utilizable");
				return;
			}
			boolean changed = false;
			if (needsDescription() && StringUtils.isNotBlank(metadata[0])) {
				author.setDescription(metadata[0]);
				recordSource(author, "description", metadata[2]);
				descriptionObtained = true;
				com.martinia.indigo.metadata.application.ProviderDiagnostics.event(provider, "Obtener descripción", "FOUND", "Se ha obtenido una descripción en español");
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
						com.martinia.indigo.metadata.application.ProviderDiagnostics.event(provider, "Descargar foto", "FOUND", "Foto descargada correctamente");
					}
					else com.martinia.indigo.metadata.application.ProviderDiagnostics.event(provider, "Descargar foto", "NOT_FOUND", "La foto indicada no está disponible; se probarán otras fuentes");
				}
				catch (RuntimeException exception) {
					com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
					failed = true;
					log.warn("Metadata provider {} photo download failed for author {} ({})",
							provider, author.getId(), author.getName(), exception);
					com.martinia.indigo.metadata.application.ProviderDiagnostics.record(provider, "Descargar foto del autor", exception);
				}
			}
			if (changed && (StringUtils.isBlank(author.getProvider()) || override && !found)) author.setProvider(metadata[2]);
			found |= changed;
			String pendingFields = needsDescription() && needsImage() ? "descripción y foto"
					: needsDescription() ? "descripción" : "foto";
			com.martinia.indigo.metadata.application.ProviderDiagnostics.event(provider, operation,
					changed ? "FOUND" : "NOT_FOUND", changed ? "Ha aportado campos pendientes"
					: "Se ha localizado información del autor, pero no aporta los campos pendientes: " + pendingFields);
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
