package com.martinia.indigo.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.martinia.indigo.model.calibre.Tag;

public class Search implements Serializable {

	private static final long serialVersionUID = 1L;
	private String title;
	private String author;
	private Date ini;
	private Date end;
	private Integer min;
	private Integer max;
	private String serie;
	private List<Tag> selectedTags;
	private String path;

	public Search() {
		super();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getIni() {
		return ini;
	}

	public void setIni(Date ini) {
		this.ini = ini;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public List<Tag> getSelectedTags() {
		return selectedTags;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public void setSelectedTags(List<Tag> selectedTags) {
		this.selectedTags = selectedTags;
	}
	
	

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isEmpty() {
		boolean ret = false;
		if (StringUtils.isEmpty(this.path) && StringUtils.isEmpty(this.title) && StringUtils.isEmpty(this.author) && ini == null && end == null
				&& min == null && max == null && serie == null && CollectionUtils.isEmpty(selectedTags)) {
			ret = true;
		}
		return ret;
	}
}
