package com.martinia.indigo.common.singletons;

import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.file.domain.model.events.UploadEpubFilesProcessFinishedEvent;
import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.domain.model.StatusEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Optional;

@Getter
@NoArgsConstructor
@Slf4j
@ToString
public class UploadEpubFilesSingleton {

	@Resource
	private EventBus eventBus;

	private boolean running;
	long total = 0;
	long extract = 0;
	long extractError = 0;
	long moveError = 0;
	long deleteError = 0;
	long newBooks = 0;
	long updatedBooks = 0;
	long newAuthors = 0;
	long newTags = 0;
	long moved = 0;
	long deleted = 0;

	public void start(final long total) {
		clear();
		this.total = total;
		this.running = true;
	}

	public void stop() {
		log.info(this.toString());
		eventBus.publish(buildEvent());
		clear();
	}

	private UploadEpubFilesProcessFinishedEvent buildEvent() {
		return UploadEpubFilesProcessFinishedEvent.builder()
				.type(NotificationEnum.UPLOAD)
				.user(getUserName())
				.date(Calendar.getInstance().getTime())
				.message(buildMessage())
				.status(StatusEnum.FINISHED)
				.total(this.total)
				.extract(this.extract)
				.extractError(this.extractError)
				.moveError(this.moveError)
				.deleteError(this.deleteError)
				.newBooks(this.newBooks)
				.updatedBooks(this.updatedBooks)
				.newAuthors(this.newAuthors)
				.newTags(this.newTags)
				.moved(this.moved)
				.deleted(this.deleted)
				.build();
	}

	private String getUserName() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.getName() != null ? auth.getName() : "system";
	}

	private String buildMessage() {
		return "Total: " + this.total + ", Extract: " + this.extract + ", Extract Error: " + this.extractError + ", Move Error: "
				+ this.moveError + ", Delete Error: " + this.deleteError + ", New Books: " + this.newBooks + ", Updated Books: "
				+ this.updatedBooks + ", New Authors: " + this.newAuthors + ", New Tags: " + this.newTags + ", Deleted: " + this.deleted;
	}

	private void clear() {
		this.running = false;
		this.total = 0;
		this.extract = 0;
		this.extractError = 0;
		this.moveError = 0;
		this.deleteError = 0;
		this.newBooks = 0;
		this.updatedBooks = 0;
		this.newAuthors = 0;
		this.newTags = 0;
		this.moved = 0;
		this.deleted = 0;
	}

	public void addUpdatedBook() {
		this.updatedBooks++;
	}

	public void addNewBook() {
		this.newBooks++;
	}

	public void addAuthor() {
		this.newAuthors++;
	}

	public void addTag() {
		this.newTags++;
	}

	public void addExtractError() {
		this.extractError++;
	}

	public void addDeleteError() {
		this.deleteError++;
	}

	public void addMoveError() {
		this.moveError++;
	}

	public void addDelete() {
		this.deleted++;
	}

	public void addMove() {
		this.moved++;
	}

	public void addExtract() {
		this.extract++;
	}

	private void check(final long current) {
		if (total <= current) {stop();}
	}

	public int getCurentStatus() {
		final long current = (deleted + deleteError + moveError + extractError);
		if (this.running && current > 0) {
			check(current);
			return (int) ((100 * current) / this.total);
		}
		else {
			return 0;
		}
	}
}
