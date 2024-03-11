package com.martinia.indigo.common.infrastructure.api.mappers;

import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import com.martinia.indigo.common.model.Review;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewDtoMapper {

	ReviewDto domain2Dto(Review domain);

	List<ReviewDto> domains2Dtos(List<Review> domains);

	Review dto2domain(ReviewDto dto);

	List<Review> dtos2domains(List<ReviewDto> dtos);

}