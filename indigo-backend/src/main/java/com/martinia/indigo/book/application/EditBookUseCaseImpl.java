package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.EditBookUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.util.ImageUtils;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Slf4j
@Transactional
public class EditBookUseCaseImpl implements EditBookUseCase {

	@Resource
	private BookRepository bookRepository;

	@Resource
	private TagRepository tagRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Resource
	private EventBus eventBus;

	@Resource
	private ImageUtils imageUtils;

	@Override
	public void edit(Book book) {
		bookRepository.findById(book.getId()).ifPresent(source -> {

			final BookMongoEntity target = bookMongoMapper.domain2Entity(book);

			if (StringUtils.isNotEmpty(target.getId()) && target.getImage().startsWith("data:") && !target.getImage().contains("null"))
				target.setImage(target.getImage().substring(target.getImage().indexOf("/9"), target.getImage().length()));

			if (target.getImage().contains("null"))
				target.setImage(null);

			if (target.getImage()==null && source.getImage()!=null)
				target.setImage(source.getImage());

			if (target.getImage()==null){
				target.setImage(imageUtils.getBase64Cover(book.getPath(), true));
			}

			bookRepository.save(target);
		});
	}

}
