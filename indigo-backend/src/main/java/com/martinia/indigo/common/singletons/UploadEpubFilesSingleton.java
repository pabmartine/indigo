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

import jakarta.annotation.Resource;
import java.util.Calendar;
import java.util.Optional;

@Getter
@NoArgsConstructor
@Slf4j
@ToString
public class UploadEpubFilesSingleton {

	@Resource
	private EventBus eventBus;

	private volatile boolean running;
	private volatile boolean managedProcessing;
	public synchronized void beginManagedProcessing() { managedProcessing = true; }
	public synchronized void endManagedProcessing() { stop(); managedProcessing = false; }
	private String user;
	volatile long total = 0;
	volatile long extract = 0;
	volatile long extractError = 0;
	volatile long moveError = 0;
	volatile long deleteError = 0;
	volatile long newBooks = 0;
	volatile long updatedBooks = 0;
	volatile long newAuthors = 0;
	volatile long newTags = 0;
	volatile long moved = 0;
	volatile long deleted = 0;

	public synchronized void start(final long total) {
		clear();
		this.total = total;
		this.user = getUserName();
		this.running = true;
	}

	public synchronized void stop() {
		if (!this.running) {
			return;
		}
		log.info(this.toString());
		eventBus.publish(buildEvent());
		this.running = false;
	}

	private UploadEpubFilesProcessFinishedEvent buildEvent() {
		return UploadEpubFilesProcessFinishedEvent.builder()
				.type(NotificationEnum.UPLOAD)
				.user(this.user != null ? this.user : "system")
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
		return "Total: " + this.total + ", Extraidos: " + this.extract + ", Movidos: " + this.moved + ", Errores de extraccion: "
				+ this.extractError + ", Errores de movimiento: " + this.moveError + ", Libros nuevos: " + this.newBooks
				+ ", Libros actualizados: " + this.updatedBooks + ", Autores nuevos: " + this.newAuthors + ", Etiquetas nuevas: "
				+ this.newTags;
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
		this.user = null;
	}

	public synchronized void addUpdatedBook() {
		this.updatedBooks++;
	}

	public synchronized void addNewBook() {
		this.newBooks++;
	}

	public synchronized void addAuthor() {
		this.newAuthors++;
	}

	public synchronized void addTag() {
		this.newTags++;
	}

	public synchronized void addExtractError() {
		this.extractError++;
	}

	public synchronized void addDeleteError() {
		this.deleteError++;
	}

	public synchronized void addMoveError() {
		this.moveError++;
	}

	public synchronized void addDelete() {
		this.deleted++;
	}

	public synchronized void addMove() {
		this.moved++;
	}

	public synchronized void addExtract() {
		this.extract++;
	}

	private void check(final long current) {
		if (!managedProcessing && total <= current) {stop();}
	}

	public synchronized int getCurentStatus() {
		final long current = moved + moveError + extractError;
		if (this.running && current > 0) {
			check(current);
			return (int) ((100 * current) / this.total);
		}
		else {
			return 0;
		}
	}

	public synchronized long getProcessedItems() {
		return moved + moveError + extractError;
	}

	public synchronized long getFailedItems() {
		return moveError + extractError;
	}
}
