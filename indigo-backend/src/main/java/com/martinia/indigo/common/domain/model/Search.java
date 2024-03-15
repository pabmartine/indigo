package com.martinia.indigo.common.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Search implements Serializable {

	private String title;
	private String author;
	private Date ini;
	private Date end;
	private Integer min;
	private Integer max;
	private String serie;
	private List<String> selectedTags;
	private String path;
	private List<String> languages;

	public boolean isEmpty() {
		boolean ret = false;
		if (StringUtils.isEmpty(this.path) && StringUtils.isEmpty(this.title) && StringUtils.isEmpty(this.author) && ini == null
				&& end == null && min == null && max == null && serie == null && CollectionUtils.isEmpty(selectedTags)) {
			ret = true;
		}
		return ret;
	}
}
