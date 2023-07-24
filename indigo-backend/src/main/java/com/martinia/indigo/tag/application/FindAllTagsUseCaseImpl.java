package com.martinia.indigo.tag.application;

import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.FindAllTagsUseCase;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import com.martinia.indigo.tag.infrastructure.mongo.mappers.TagMongoMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
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

		tags.forEach(tag -> {
			int total = 0;
			for (String key : tag.getNumBooks().getLanguages().keySet()) {
				if (languages.contains(key)) {
					total += tag.getNumBooks().getLanguages().get(key);
				}
			}
			tag.getNumBooks().setTotal(total);
		});

		return tagMongoMapper.entities2Domains(tags);
	}

}
