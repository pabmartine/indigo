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

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

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
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.metadata.application.MetadataActivityService activity;

	@Override
	public void edit(Book book) {
		bookRepository.findById(book.getId()).ifPresent(source -> {

			final BookMongoEntity target = bookMongoMapper.domain2Entity(book);
			preserveManagedMetadata(source, target);

			if (StringUtils.isNotEmpty(target.getId()) && target.getImage() != null && target.getImage().startsWith("data:") && !target.getImage().contains("null"))
				target.setImage(target.getImage().substring(target.getImage().indexOf("/9"), target.getImage().length()));

			if (target.getImage() != null && target.getImage().contains("null"))
				target.setImage(null);

			if (target.getImage()==null && source.getImage()!=null)
				target.setImage(source.getImage());

			if (target.getImage()==null){
				target.setImage(imageUtils.getBase64Cover(book.getPath(), true));
			}

			bookRepository.save(target);
			if (activity != null) {
				activity.lock("BOOKS", source.getId(), true);
			}
		});
	}

	private void preserveManagedMetadata(final BookMongoEntity source, final BookMongoEntity target) {
		target.setIsbn10(source.getIsbn10());
		target.setIsbn13(source.getIsbn13());
		target.setIdentifiers(source.getIdentifiers());
		target.setOpenLibraryWorkId(source.getOpenLibraryWorkId());
		target.setOpenLibraryEditionId(source.getOpenLibraryEditionId());
		target.setRatingAverage(source.getRatingAverage());
		target.setRatingsCount(source.getRatingsCount());
		target.setRatingDistribution(source.getRatingDistribution());
		target.setRatingProvider(source.getRatingProvider());
		target.setRatingUpdatedAt(source.getRatingUpdatedAt());
		target.setMetadataMatchStatus(source.getMetadataMatchStatus());
		target.setMetadataMatchConfidence(source.getMetadataMatchConfidence());
		target.setLastMetadataSync(source.getLastMetadataSync());
		target.setLastReviewsMetadataSync(source.getLastReviewsMetadataSync());
		target.setReviewsMetadataStatus(source.getReviewsMetadataStatus());
		target.setReviewsMetadataError(source.getReviewsMetadataError());
		target.setReviews(source.getReviews());
	}

}
