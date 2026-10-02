package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

class MetadataConcurrentPersistenceIntegrationTest extends BaseIndigoTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void staleBookAndReviewSnapshotsCannotOverwriteEachOther(boolean reviewsFirst) {
        var initial = bookRepository.save(BookMongoEntity.builder().title("Original").build());
        var metadata = bookRepository.findById(initial.getId()).orElseThrow();
        var reviews = bookRepository.findById(initial.getId()).orElseThrow();
        metadata.setRatingAverage(4.5F);
        metadata.setRating(4.5F);
        metadata.setRatingProvider("OPEN_LIBRARY");
        metadata.setLastMetadataSync(new Date());
        reviews.setReviews(List.of(ReviewMongo.builder().name("Reader").comment("Review").build()));
        reviews.setReviewsMetadataStatus("FOUND");
        reviews.setLastReviewsMetadataSync(new Date());
        initial.setTitle("Edited concurrently");
        bookRepository.save(initial);
        if (reviewsFirst) {
            bookRepository.updateReviewMetadata(reviews);
            bookRepository.updateBookMetadata(metadata);
        } else {
            bookRepository.updateBookMetadata(metadata);
            bookRepository.updateReviewMetadata(reviews);
        }
        var saved = bookRepository.findById(initial.getId()).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("Edited concurrently");
        assertThat(saved.getRatingAverage()).isEqualTo(4.5F);
        assertThat(saved.getRatingProvider()).isEqualTo("OPEN_LIBRARY");
        assertThat(saved.getReviews()).singleElement().satisfies(review -> assertThat(review.getComment()).isEqualTo("Review"));
        assertThat(saved.getLastMetadataSync()).isNotNull();
        assertThat(saved.getLastReviewsMetadataSync()).isNotNull();
        assertThat(saved.getReviewsMetadataStatus()).isEqualTo("FOUND");
    }
}
