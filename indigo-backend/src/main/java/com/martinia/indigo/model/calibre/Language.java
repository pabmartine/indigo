package com.martinia.indigo.model.calibre;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the "languages" database table.
 * 
 */
@Entity
@Table(name="\"languages\"")
@NamedQuery(name="Language.findAll", query="SELECT l FROM Language l")
public class Language implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="\"id\"")
	private int id;

	@Column(name="\"lang_code\"")
	private String langCode;

	public Language() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLangCode() {
		return this.langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

}