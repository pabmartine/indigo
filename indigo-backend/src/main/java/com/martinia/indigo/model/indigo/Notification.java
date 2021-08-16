package com.martinia.indigo.model.indigo;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.martinia.indigo.enums.NotificationEnum;
import com.martinia.indigo.enums.StatusEnum;

/**
 * The persistent class for the "notifications" database table.
 * 
 */
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

	public Notification() {
	}

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

	public int getUser() {
		return user;
	}

	public void setUser(int user) {
		this.user = user;
	}

	public int getBook() {
		return this.book;
	}

	public void setBook(int book) {
		this.book = book;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public NotificationEnum getType() {
		return type;
	}

	public void setType(NotificationEnum type) {
		this.type = type;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public boolean isReadByUser() {
		return readByUser;
	}

	public void setReadByUser(boolean readByUser) {
		this.readByUser = readByUser;
	}

	public boolean isReadByAdmin() {
		return readByAdmin;
	}

	public void setReadByAdmin(boolean readByAdmin) {
		this.readByAdmin = readByAdmin;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getSendDate() {
		return sendDate;
	}

	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}

}