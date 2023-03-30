package com.martinia.indigo.adapters.in.rest.mappers;

import com.martinia.indigo.adapters.in.rest.dtos.inner.ReviewDto;
import com.martinia.indigo.domain.model.inner.Review;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewDtoMapper {

	ReviewDto domain2Dto(Review domain);

	List<ReviewDto> domains2Dtos(List<Review> domains);

}