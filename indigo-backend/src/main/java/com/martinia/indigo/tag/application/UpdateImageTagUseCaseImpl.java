package com.martinia.indigo.tag.application;

import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.domain.model.commands.FindImageTagMetadataCommand;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.UpdateImageTagUseCase;
import com.martinia.indigo.tag.infrastructure.mongo.mappers.TagMongoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
@Slf4j
public class UpdateImageTagUseCaseImpl implements UpdateImageTagUseCase {

	@Resource
	private TagRepository tagRepository;

	@Resource
	private CommandBus commandBus;

	@Resource
	private TagMongoMapper tagMongoMapper;

	@Override
	public Tag updateImage(final String source) {
		return tagRepository.findById(source).map(entitySource -> {
			entitySource.setImage(commandBus.executeAndWait(FindImageTagMetadataCommand.builder().tag(entitySource.getName()).build()));
			return tagMongoMapper.entity2Domain(tagRepository.save(entitySource));
		}).orElseGet(() -> null);
	}

}
