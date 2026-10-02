package com.martinia.indigo.metadata.application.openlibrary;

import com.martinia.indigo.metadata.application.libretranslate.CachedSpanishTranslation;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.ports.repositories.OpenLibraryAuthorRepository;
import com.martinia.indigo.metadata.domain.ports.repositories.OpenLibraryIndexJobRepository;
import com.martinia.indigo.metadata.domain.ports.usecases.openlibrary.FindOpenLibraryAuthorCatalogUseCase;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "flags.openlibrary", havingValue = "true")
public class FindOpenLibraryAuthorCatalogUseCaseImpl implements FindOpenLibraryAuthorCatalogUseCase {
	@Resource(name = "openLibraryAuthorRepository")
	private OpenLibraryAuthorRepository authorRepository;
	@Resource
	private OpenLibraryIndexJobRepository jobRepository;
	@Resource
	private CachedSpanishTranslation spanishTranslation;
	@Value("${metadata.openlibrary.author-image}")
	private String authorImageEndpoint;

	@Override
	public String[] findAuthor(final String name) {
		if (StringUtils.isBlank(name)) return null;
		var job = jobRepository.findById(OpenLibraryIndexManager.JOB_ID).orElse(null);
		if (job == null || job.getActiveVersion() == null || !job.getActiveVersion().equals(job.getAuthorsVersion())) return null;
		var matches = authorRepository.findTop2ByIndexVersionAndNames(job.getActiveVersion(), AuthorNameNormalizer.normalize(name));
		if (matches.size() != 1) return null;
		var author = matches.get(0);
		String translated = null;
		if (StringUtils.isNotBlank(author.getBiography())) {
			translated = spanishTranslation.translate(author.getBiography());
			if (StringUtils.isBlank(translated)) {
				throw new AuthorCatalogTranslationException("Spanish author translation unavailable; retry later");
			}
		}
		String image = author.isHasPhoto() ? authorImageEndpoint.replace("$id", author.getAuthorId()) : null;
		return new String[] { translated, image, ProviderEnum.OPEN_LIBRARY.name() };
	}
}
