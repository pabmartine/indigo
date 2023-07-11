package com.martinia.indigo.common.infrastructure.mongo.mappers;

import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import com.martinia.indigo.common.infrastructure.mongo.entities.ViewMongoEntity;
import com.martinia.indigo.common.model.Review;
import com.martinia.indigo.common.model.View;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMongoMapper {

	ReviewMongo domain2Entity(Review domain);

	List<ReviewMongo> domains2Entities(List<Review> domain);

}