package com.martinia.indigo.tag.application;

import com.martinia.indigo.common.util.LanguageCodeUtils;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.FindAllTagsUseCase;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import com.martinia.indigo.tag.infrastructure.mongo.mappers.TagMongoMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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

	private List<Tag> mapTags(List<String> languages, List<TagMongoEntity> tags) {
		tags.forEach(tag -> {
			if (languages == null || languages.isEmpty()) {
				tag.getNumBooks().setTotal(tag.getNumBooks().getLanguages().values().stream().mapToInt(Integer::intValue).sum());
				return;
			}
			int total = 0;
			for (String key : tag.getNumBooks().getLanguages().keySet()) {
				if (languages.stream().anyMatch(language -> LanguageCodeUtils.variants(language).contains(key))) {
					total += tag.getNumBooks().getLanguages().get(key);
				}
			}
			tag.getNumBooks().setTotal(total);
		});
		return tagMongoMapper.entities2Domains(tags);
	}
}
