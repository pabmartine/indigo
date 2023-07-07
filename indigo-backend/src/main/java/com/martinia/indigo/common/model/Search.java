package com.martinia.indigo.common.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Search implements Serializable {

	private static final long serialVersionUID = 6262771168466287057L;
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
		if (StringUtils.isEmpty(this.path)
				&& StringUtils.isEmpty(this.title)
				&& StringUtils.isEmpty(this.author)
				&& ini == null
				&& end == null
				&& min == null
				&& max == null
				&& serie == null
				&& CollectionUtils.isEmpty(selectedTags)
		) {
			ret = true;
		}
		return ret;
	}
}
