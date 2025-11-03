package com.martinia.indigo.notification.infrastructure.event;

import com.martinia.indigo.file.domain.model.events.EmailSendedEvent;
import com.martinia.indigo.file.domain.model.events.UploadEpubFilesProcessFinishedEvent;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.domain.model.StatusEnum;
import com.martinia.indigo.notification.domain.model.events.NotificationEvent;
import com.martinia.indigo.notification.domain.ports.usecases.SaveNotificationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SaveNotificationEventListenerTest {

    @Mock
    private SaveNotificationUseCase saveNotificationUseCase;

    @InjectMocks
    private SaveNotificationEventListener listener;

    private Date testDate;

    @BeforeEach
    void setUp() {
        testDate = new Date();
    }

    @Test
    void handle_WithUploadEpubFilesProcessFinishedEvent_ShouldSaveNotificationWithUploadDetails() {
        // Given
        UploadEpubFilesProcessFinishedEvent event = UploadEpubFilesProcessFinishedEvent.builder()
                .id("event-123")
                .type(NotificationEnum.UPLOAD)
                .user("testUser")
                .date(testDate)
                .readUser(false)
                .readAdmin(true)
                .status(StatusEnum.FINISHED)
                .message("Upload process completed")
                .total(100L)
                .extract(95L)
                .extractError(5L)
                .moveError(2L)
                .deleteError(1L)
                .newBooks(20L)
                .updatedBooks(10L)
                .newAuthors(5L)
                .newTags(8L)
                .moved(15L)
                .deleted(3L)
                .build();

        // When
        listener.handle(event);

        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(saveNotificationUseCase).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertThat(savedNotification.getId()).isEqualTo("event-123");
        assertThat(savedNotification.getType()).isEqualTo(NotificationEnum.UPLOAD);
        assertThat(savedNotification.getUser()).isEqualTo("testUser");
        assertThat(savedNotification.getDate()).isEqualTo(testDate);
        assertThat(savedNotification.isReadUser()).isFalse();
        assertThat(savedNotification.isReadAdmin()).isTrue();
        assertThat(savedNotification.getStatus()).isEqualTo(StatusEnum.FINISHED);
        assertThat(savedNotification.getMessage()).isEqualTo("Upload process completed");

        // Check upload details
        assertThat(savedNotification.getUpload()).isNotNull();
        assertThat(savedNotification.getUpload().getTotal()).isEqualTo(100L);
        assertThat(savedNotification.getUpload().getExtract()).isEqualTo(95L);
        assertThat(savedNotification.getUpload().getExtractError()).isEqualTo(5L);
        assertThat(savedNotification.getUpload().getMoveError()).isEqualTo(2L);
        assertThat(savedNotification.getUpload().getDeleteError()).isEqualTo(1L);
        assertThat(savedNotification.getUpload().getNewBooks()).isEqualTo(20L);
        assertThat(savedNotification.getUpload().getUpdatedBooks()).isEqualTo(10L);
        assertThat(savedNotification.getUpload().getNewAuthors()).isEqualTo(5L);
        assertThat(savedNotification.getUpload().getNewTags()).isEqualTo(8L);
        assertThat(savedNotification.getUpload().getMoved()).isEqualTo(15L);
        assertThat(savedNotification.getUpload().getDeleted()).isEqualTo(3L);

        // Check that kindle is null
        assertThat(savedNotification.getKindle()).isNull();
    }

    @Test
    void handle_WithEmailSendedEvent_ShouldSaveNotificationWithKindleDetails() {
        // Given
        EmailSendedEvent event = EmailSendedEvent.builder()
                .id("email-456")
                .type(NotificationEnum.KINDLE)
                .user("kindleUser")
                .date(testDate)
                .readUser(true)
                .readAdmin(false)
                .status(StatusEnum.NOT_SEND)
                .message("Email sending failed")
                .book("Test Book Title")
                .build();

        // When
        listener.handle(event);

        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(saveNotificationUseCase).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertThat(savedNotification.getId()).isEqualTo("email-456");
        assertThat(savedNotification.getType()).isEqualTo(NotificationEnum.KINDLE);
        assertThat(savedNotification.getUser()).isEqualTo("kindleUser");
        assertThat(savedNotification.getDate()).isEqualTo(testDate);
        assertThat(savedNotification.isReadUser()).isTrue();
        assertThat(savedNotification.isReadAdmin()).isFalse();
        assertThat(savedNotification.getStatus()).isEqualTo(StatusEnum.NOT_SEND);
        assertThat(savedNotification.getMessage()).isEqualTo("Email sending failed");

        // Check kindle details
        assertThat(savedNotification.getKindle()).isNotNull();
        assertThat(savedNotification.getKindle().getBook()).isEqualTo("Test Book Title");

        // Check that upload is null
        assertThat(savedNotification.getUpload()).isNull();
    }

    @Test
    void handle_WithUnknownNotificationEvent_ShouldSaveNullNotification() {
        // Given - Custom notification event that doesn't match any specific type
        NotificationEvent unknownEvent = NotificationEvent.builder()
                .id("unknown-789")
                .type(NotificationEnum.UPLOAD)
                .user("unknownUser")
                .date(testDate)
                .readUser(false)
                .readAdmin(false)
                .status(StatusEnum.SEND)
                .message("Unknown event type")
                .build();

        // When
        listener.handle(unknownEvent);

        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(saveNotificationUseCase).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertNull(savedNotification);
    }

    @Test
    void handle_WithUploadEventWithMinimalData_ShouldHandleCorrectly() {
        // Given
        UploadEpubFilesProcessFinishedEvent event = UploadEpubFilesProcessFinishedEvent.builder()
                .id("minimal-event")
                .type(NotificationEnum.UPLOAD)
                .user("user")
                .date(testDate)
                .readUser(false)
                .readAdmin(false)
                .status(StatusEnum.SEND)
                .message("Minimal event")
                .total(0L)
                .extract(0L)
                .extractError(0L)
                .moveError(0L)
                .deleteError(0L)
                .newBooks(0L)
                .updatedBooks(0L)
                .newAuthors(0L)
                .newTags(0L)
                .moved(0L)
                .deleted(0L)
                .build();

        // When
        listener.handle(event);

        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(saveNotificationUseCase).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertThat(savedNotification).isNotNull();
        assertThat(savedNotification.getUpload()).isNotNull();
        assertThat(savedNotification.getUpload().getTotal()).isEqualTo(0L);
    }

    @Test
    void handle_WithEmailEventWithEmptyBook_ShouldHandleCorrectly() {
        // Given
        EmailSendedEvent event = EmailSendedEvent.builder()
                .id("empty-book")
                .type(NotificationEnum.KINDLE)
                .user("user")
                .date(testDate)
                .readUser(false)
                .readAdmin(false)
                .status(StatusEnum.FINISHED)
                .message("Book sent successfully")
                .book("")
                .build();

        // When
        listener.handle(event);

        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(saveNotificationUseCase).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertThat(savedNotification).isNotNull();
        assertThat(savedNotification.getKindle()).isNotNull();
        assertThat(savedNotification.getKindle().getBook()).isEmpty();
    }
}