package com.martinia.indigo.mail.application;

import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.file.domain.model.events.EmailSendedEvent;
import com.martinia.indigo.mail.domain.EmailConfiguration;
import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.domain.model.StatusEnum;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendMailUseCaseImplUnitTest {

	@Mock
	private ConfigurationRepository configurationRepository;

	@Spy
	private JavaMailSenderImpl javaMailSender;

	@Mock
	private EventBus eventBus;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	@Mock
	private MimeMessage mimeMessage;

	@InjectMocks
	private SendMailUseCaseImpl sendMailUseCase;

	@TempDir
	Path tempDir;

	@Test
	void mail_WhenPathContainsSpecialCharacters_ShouldReplaceThemAndSendEmail() throws IOException {
		when(configurationRepository.findByKey("smtp.host"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.host").value("smtp.test.com").build()));
		when(configurationRepository.findByKey("smtp.port"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.port").value("587").build()));
		when(configurationRepository.findByKey("smtp.username"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.username").value("test@test.com").build()));
		when(configurationRepository.findByKey("smtp.password"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.password").value("password").build()));
		when(configurationRepository.findByKey("smtp.encryption"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.encryption").value("starttls").build()));
		doReturn(mimeMessage).when(javaMailSender).createMimeMessage();
		doNothing().when(javaMailSender).send(any(MimeMessage.class));

		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn("testuser");

		String specialPath = "test&folder[file]name`";
		String testPath = tempDir.resolve(specialPath).toString();
		String address = "test@example.com";

		File testFolder = new File(testPath);
		testFolder.mkdirs();

		File epubFile = new File(testFolder, "test.epub");
		Files.createFile(epubFile.toPath());

		String mailPath = testPath.replace("&", "@_@").replace("[", "@-@").replace("]", "@¡@").replace("`", "@!@");

		String result = sendMailUseCase.mail(mailPath, address);

		verify(eventBus).publish(any(EmailSendedEvent.class));
		assertThat(result).isNull();
	}

	@Test
	void mail_WhenEpubFileExists_ShouldSendEmailSuccessfully() throws IOException {
		when(configurationRepository.findByKey("smtp.host"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.host").value("smtp.test.com").build()));
		when(configurationRepository.findByKey("smtp.port"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.port").value("587").build()));
		when(configurationRepository.findByKey("smtp.username"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.username").value("test@test.com").build()));
		when(configurationRepository.findByKey("smtp.password"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.password").value("password").build()));
		when(configurationRepository.findByKey("smtp.encryption"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.encryption").value("starttls").build()));
		doReturn(mimeMessage).when(javaMailSender).createMimeMessage();
		doNothing().when(javaMailSender).send(any(MimeMessage.class));

		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn("testuser");

		String testPath = tempDir.toString() + "/testbook";
		String address = "test@example.com";

		File testFolder = new File(testPath);
		testFolder.mkdirs();

		File epubFile = new File(testFolder, "testbook.epub");
		Files.createFile(epubFile.toPath());

		String result = sendMailUseCase.mail(testPath, address);

		verify(eventBus).publish(any(EmailSendedEvent.class));
		assertThat(result).isNull();
	}

	@Test
	void mail_WhenEpubFileNotFound_ShouldReturnErrorAndPublishEvent() throws IOException {
		when(configurationRepository.findByKey("smtp.host"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.host").value("smtp.test.com").build()));
		when(configurationRepository.findByKey("smtp.port"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.port").value("587").build()));
		when(configurationRepository.findByKey("smtp.username"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.username").value("test@test.com").build()));
		when(configurationRepository.findByKey("smtp.password"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.password").value("password").build()));
		when(configurationRepository.findByKey("smtp.encryption"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.encryption").value("starttls").build()));

		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn("testuser");

		String testPath = tempDir.toString() + "/testbook";
		String address = "test@example.com";

		File testFolder = new File(testPath);
		testFolder.mkdirs();

		File txtFile = new File(testFolder, "testbook.txt");
		Files.createFile(txtFile.toPath());

		String result = sendMailUseCase.mail(testPath, address);

		verify(eventBus).publish(argThat(event ->
					event instanceof EmailSendedEvent &&
							((EmailSendedEvent) event).getStatus() == StatusEnum.NOT_SEND &&
							((EmailSendedEvent) event).getMessage().contains("Epub file not found")));

		assertThat(result).contains("Epub file not found");
	}

	@Test
	void mail_WhenPathNotExists_ShouldReturnErrorAndPublishEvent() {
		when(configurationRepository.findByKey("smtp.host"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.host").value("smtp.test.com").build()));
		when(configurationRepository.findByKey("smtp.port"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.port").value("587").build()));
		when(configurationRepository.findByKey("smtp.username"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.username").value("test@test.com").build()));
		when(configurationRepository.findByKey("smtp.password"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.password").value("password").build()));
		when(configurationRepository.findByKey("smtp.encryption"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.encryption").value("starttls").build()));

		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn("testuser");

		String testPath = "/non/existent/path";
		String address = "test@example.com";

		String result = sendMailUseCase.mail(testPath, address);

		verify(eventBus).publish(argThat(event ->
					event instanceof EmailSendedEvent &&
							((EmailSendedEvent) event).getStatus() == StatusEnum.NOT_SEND &&
							((EmailSendedEvent) event).getMessage().contains("Path to") &&
							((EmailSendedEvent) event).getMessage().contains("not exist")));

		assertThat(result).contains("not exist");
	}

	@Test
	void mail_WhenMailExceptionThrown_ShouldReturnErrorAndPublishEvent() throws IOException {
		when(configurationRepository.findByKey("smtp.host"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.host").value("smtp.test.com").build()));
		when(configurationRepository.findByKey("smtp.port"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.port").value("587").build()));
		when(configurationRepository.findByKey("smtp.username"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.username").value("test@test.com").build()));
		when(configurationRepository.findByKey("smtp.password"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.password").value("password").build()));
		when(configurationRepository.findByKey("smtp.encryption"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.encryption").value("starttls").build()));

		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn("testuser");

		String testPath = tempDir.toString() + "/testbook";
		String address = "test@example.com";

		File testFolder = new File(testPath);
		testFolder.mkdirs();

		File epubFile = new File(testFolder, "testbook.epub");
		Files.createFile(epubFile.toPath());

		doThrow(new MailSendException("Mail server error")).when(javaMailSender).send(any(MimeMessage.class));

		String result = sendMailUseCase.mail(testPath, address);

		verify(eventBus).publish(argThat(event ->
					event instanceof EmailSendedEvent &&
							((EmailSendedEvent) event).getStatus() == StatusEnum.NOT_SEND &&
							((EmailSendedEvent) event).getMessage().equals("Mail server error")));

		assertThat(result).isEqualTo("Mail server error");
	}

	@Test
	void getEmailConfig_ShouldReturnConfigurationFromRepository() {
		when(configurationRepository.findByKey("smtp.host"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.host").value("smtp.test.com").build()));
		when(configurationRepository.findByKey("smtp.port"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.port").value("587").build()));
		when(configurationRepository.findByKey("smtp.username"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.username").value("test@test.com").build()));
		when(configurationRepository.findByKey("smtp.password"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.password").value("password").build()));
		when(configurationRepository.findByKey("smtp.encryption"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.encryption").value("starttls").build()));

		EmailConfiguration result = sendMailUseCase.getEmailConfig();

		assertThat(result.getHost()).isEqualTo("smtp.test.com");
		assertThat(result.getPort()).isEqualTo(587);
		assertThat(result.getUsername()).isEqualTo("test@test.com");
		assertThat(result.getPassword()).isEqualTo("password");
		assertThat(result.getEncryption()).isEqualTo("starttls");
	}

	@Test
	void getEmailConfig_WhenConfigurationNotFound_ShouldReturnDefaultValues() {
		when(configurationRepository.findByKey(anyString())).thenReturn(Optional.empty());

		EmailConfiguration result = sendMailUseCase.getEmailConfig();

		assertThat(result.getHost()).isNull();
		assertThat(result.getPort()).isEqualTo(0);
		assertThat(result.getUsername()).isNull();
		assertThat(result.getPassword()).isNull();
		assertThat(result.getEncryption()).isNull();
	}

	@Test
	void buildEvent_ShouldCreateEmailSendedEventWithCorrectProperties() throws IOException {
		when(configurationRepository.findByKey("smtp.host"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.host").value("smtp.test.com").build()));
		when(configurationRepository.findByKey("smtp.port"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.port").value("587").build()));
		when(configurationRepository.findByKey("smtp.username"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.username").value("test@test.com").build()));
		when(configurationRepository.findByKey("smtp.password"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.password").value("password").build()));
		when(configurationRepository.findByKey("smtp.encryption"))
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("smtp.encryption").value("starttls").build()));
		doReturn(mimeMessage).when(javaMailSender).createMimeMessage();
		doNothing().when(javaMailSender).send(any(MimeMessage.class));

		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn("testuser");

		String testPath = tempDir.toString() + "/testbook";
		String address = "test@example.com";

		File testFolder = new File(testPath);
		testFolder.mkdirs();

		File epubFile = new File(testFolder, "testbook.epub");
		Files.createFile(epubFile.toPath());

		sendMailUseCase.mail(testPath, address);

		verify(eventBus).publish(argThat(event -> {
			if (!(event instanceof EmailSendedEvent)) return false;
			EmailSendedEvent emailEvent = (EmailSendedEvent) event;
			return emailEvent.getType() == NotificationEnum.KINDLE &&
				   emailEvent.getUser().equals("testuser") &&
				   emailEvent.getBook().equals(testPath) &&
				   emailEvent.getStatus() == StatusEnum.SEND &&
				   emailEvent.getMessage() == null;
		}));
	}
}