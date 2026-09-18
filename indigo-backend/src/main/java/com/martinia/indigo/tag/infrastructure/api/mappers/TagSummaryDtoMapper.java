package com.martinia.indigo.tag.infrastructure.api.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.model.TagPageData;
import com.martinia.indigo.tag.infrastructure.api.model.TagSummaryDto;
import com.martinia.indigo.tag.infrastructure.api.model.TagSummaryPageDto;

@Mapper(componentModel = "spring")
public interface TagSummaryDtoMapper {

	@Mappings({
			@Mapping(source = "numBooks.total", target = "numBooks")
	})
	TagSummaryDto domain2Dto(Tag domain);

	List<TagSummaryDto> domains2Dtos(List<Tag> domains);

	default TagSummaryPageDto pageData2Dto(TagPageData pageData) {
		if (pageData == null) {
			return null;
		}
		return new TagSummaryPageDto(
				domains2Dtos(pageData.items()),
				pageData.total(),
				pageData.page(),
				pageData.size()
		);
	}
}
