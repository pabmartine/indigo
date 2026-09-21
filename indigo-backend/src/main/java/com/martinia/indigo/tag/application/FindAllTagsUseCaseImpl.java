package com.martinia.indigo.tag.application;

import com.martinia.indigo.common.util.LanguageCodeUtils;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.FindAllTagsUseCase;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import com.martinia.indigo.tag.domain.model.TagPageData;
import com.martinia.indigo.tag.infrastructure.mongo.mappers.TagMongoMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FindAllTagsUseCaseImpl implements FindAllTagsUseCase {

	@Resource
	private TagRepository tagRepository;

	@Resource
	private TagMongoMapper tagMongoMapper;

	@Override
	public List<Tag> findAll(final List<String> languages, final String sort, final String order) {
		List<TagMongoEntity> tags = tagRepository.findAll(languages, Sort.by(Sort.Direction.fromString(order), sort));
		return mapTags(languages, tags);
	}

	@Override
	public List<Tag> findAll(final List<String> languages, final int page, final int size, final String sort, final String order) {
		List<TagMongoEntity> tags = tagRepository.findAll(languages, page, size, Sort.by(Sort.Direction.fromString(order), sort));
		return mapTags(languages, tags);
	}

	@Override
	public TagPageData findSummaryPage(final List<String> languages, final int page, final int size, final String sort, final String order) {
		Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
		String sortField = resolveSortField(sort);
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
		return tagRepository.findSummaryPage(languages, pageRequest);
	}

	private String resolveSortField(String sort) {
		if (sort == null || sort.isBlank() || "name".equalsIgnoreCase(sort)) {
			return "name";
		}
		if ("numBooks".equalsIgnoreCase(sort) || "numBooks.total".equalsIgnoreCase(sort)) {
			return "numBooks.total";
		}
		if ("id".equalsIgnoreCase(sort)) {
			return "_id";
		}
		return sort;
	}


	private List<Tag> mapTags(List<String> languages, List<TagMongoEntity> tags) {
		java.util.Set<String> requestedVariants = (languages == null || languages.isEmpty())
				? java.util.Collections.emptySet()
				: languages.stream().flatMap(l -> LanguageCodeUtils.variants(l).stream()).collect(Collectors.toSet());

		tags.forEach(tag -> {
			if (tag.getNumBooks() == null || tag.getNumBooks().getLanguages() == null) {
				return;
			}
			if (requestedVariants.isEmpty()) {
				tag.getNumBooks().setTotal(tag.getNumBooks().getLanguages().values().stream().mapToInt(Integer::intValue).sum());
				return;
			}
			int total = tag.getNumBooks().getLanguages().entrySet().stream()
					.filter(e -> requestedVariants.contains(e.getKey()))
					.mapToInt(java.util.Map.Entry::getValue).sum();
			tag.getNumBooks().setTotal(total);
		});
		return tagMongoMapper.entities2Domains(tags);
	}
}
