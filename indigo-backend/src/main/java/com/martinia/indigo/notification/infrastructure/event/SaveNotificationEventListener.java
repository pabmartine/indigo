package com.martinia.indigo.notification.infrastructure.event;

import com.martinia.indigo.common.bus.event.domain.model.EventBusListener;
import com.martinia.indigo.file.domain.model.events.EmailSendedEvent;
import com.martinia.indigo.file.domain.model.events.UploadEpubFilesProcessFinishedEvent;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.model.NotificationEpubFileUploadItem;
import com.martinia.indigo.notification.domain.model.NotificationKindleItem;
import com.martinia.indigo.notification.domain.model.events.NotificationEvent;
import com.martinia.indigo.notification.domain.ports.usecases.SaveNotificationUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SaveNotificationEventListener extends EventBusListener<NotificationEvent> {

	@Resource
	private SaveNotificationUseCase saveNotificationUseCase;

	@Override
	public void handle(final NotificationEvent event) {
		final Notification notification = buildNotification(event);
		saveNotificationUseCase.save(notification);
	}

	private Notification buildNotification(final NotificationEvent event) {
		if (event instanceof UploadEpubFilesProcessFinishedEvent) {
			return Notification.builder()
					.id(event.getId())
					.type(event.getType())
					.user(event.getUser())
					.date(event.getDate())
					.readUser(event.isReadUser())
					.readAdmin(event.isReadAdmin())
					.status(event.getStatus())
					.message(event.getMessage())
					.upload(buildUploads((UploadEpubFilesProcessFinishedEvent) event))
					.build();
		} else if (event instanceof EmailSendedEvent) {
			return Notification.builder()
					.id(event.getId())
					.type(event.getType())
					.user(event.getUser())
					.date(event.getDate())
					.readUser(event.isReadUser())
					.readAdmin(event.isReadAdmin())
					.status(event.getStatus())
					.message(event.getMessage())
					.kindle(buildKindle((EmailSendedEvent) event))
					.build();
		}
		return null;
	}

	private NotificationEpubFileUploadItem buildUploads(final UploadEpubFilesProcessFinishedEvent event) {
		return NotificationEpubFileUploadItem.builder()
				.total(event.getTotal())
				.extract(event.getExtract())
				.extractError(event.getExtractError())
				.moveError(event.getMoveError())
				.deleteError(event.getDeleteError())
				.newBooks(event.getNewBooks())
				.updatedBooks(event.getUpdatedBooks())
				.newAuthors(event.getNewAuthors())
				.newTags(event.getNewTags())
				.moved(event.getMoved())
				.deleted(event.getDeleted())
				.build();
	}

	private NotificationKindleItem buildKindle(final EmailSendedEvent event) {
		return NotificationKindleItem.builder()
				.book(event.getBook())
				.build();
	}
}