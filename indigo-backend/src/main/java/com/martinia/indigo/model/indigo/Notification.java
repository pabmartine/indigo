package com.martinia.indigo.model.indigo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.martinia.indigo.enums.NotificationEnum;
import com.martinia.indigo.enums.StatusEnum;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the "notifications" database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "\"notifications\"")
@NamedQuery(name = "Notification.findAll", query = "SELECT b FROM Notification b")
public class Notification implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"id\"")
	private int id;

	@Column(name = "\"type\"")
	@Enumerated(EnumType.STRING)
	private NotificationEnum type;

	@Column(name = "\"user\"")
	private int user;

	@Column(name = "\"book\"")
	private int book;

	@Column(name = "\"status\"")
	@Enumerated(EnumType.STRING)
	private StatusEnum status;

	@Column(name = "\"error\"")
	private String error;

	@Column(name = "\"read_by_user\"")
	@Type(type = "numeric_boolean")
	private boolean readByUser;

	@Column(name = "\"read_by_admin\"")
	@Type(type = "numeric_boolean")
	private boolean readByAdmin;

	@Column(name = "\"send_date\"")
	private String sendDate;

	public Notification(NotificationEnum type, int user, int book, StatusEnum status, boolean readByUser,
			boolean readByAdmin, String error) {
		super();
		this.type = type;
		this.user = user;
		this.book = book;
		this.status = status;
		this.readByUser = readByUser;
		this.readByAdmin = readByAdmin;
		this.error = error;
	}

}