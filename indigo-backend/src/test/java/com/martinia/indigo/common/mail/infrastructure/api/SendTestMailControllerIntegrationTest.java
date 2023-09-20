package com.martinia.indigo.common.mail.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SendTestMailControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private MockMvc mockMvc;
	@Resource
	private JavaMailSender javaMailSenderMock;

	@Value("${book.library.path}")
	@Setter
	protected String libraryPath;

	@BeforeEach
	public void init() {
		JavaMailSenderImpl ms = (JavaMailSenderImpl) javaMailSenderMock;
		Mockito.when(ms.getJavaMailProperties()).thenReturn(Mockito.mock(Properties.class));
		//create db properties
		configurationRepository.save(ConfigurationMongoEntity.builder().key("smtp.host").value("host").build());
		configurationRepository.save(ConfigurationMongoEntity.builder().key("smtp.port").value("80").build());
		configurationRepository.save(ConfigurationMongoEntity.builder().key("smtp.username").value("username").build());
		configurationRepository.save(ConfigurationMongoEntity.builder().key("smtp.password").value("password").build());
		configurationRepository.save(ConfigurationMongoEntity.builder().key("smtp.encryption").value("starttls").build());
	}

	@Test
	public void sendMailTest() throws Exception {
		// Given
		String address = "test@mail.com";

		Mockito.when(javaMailSenderMock.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage.class));
		Mockito.doNothing().when(javaMailSenderMock).send(Mockito.any(MimeMessage.class));

		// When
		ResultActions result = mockMvc.perform(get("/api/mail/test").param("address", address).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk());

	}

	@Test
	public void sendMailTestEncryptionNotConfigured() throws Exception {
		// Given
		String address = "test@mail.com";

		configurationRepository.save(ConfigurationMongoEntity.builder().key("smtp.encryption").value(null).build());

		Mockito.when(javaMailSenderMock.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage.class));
		Mockito.doNothing().when(javaMailSenderMock).send(Mockito.any(MimeMessage.class));

		// When
		ResultActions result = mockMvc.perform(get("/api/mail/test").param("address", address).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk());

	}

	@Test
	public void sendMailTestOtherEncryption() throws Exception {
		// Given
		String address = "test@mail.com";

		configurationRepository.save(ConfigurationMongoEntity.builder().key("smtp.encryption").value("ssl/tls").build());

		Mockito.when(javaMailSenderMock.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage.class));
		Mockito.doNothing().when(javaMailSenderMock).send(Mockito.any(MimeMessage.class));

		// When
		ResultActions result = mockMvc.perform(get("/api/mail/test").param("address", address).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk());

	}

	@Test
	public void sendMailTestError() throws Exception {
		// Given
		String address = "test@mail.com";

		Mockito.when(javaMailSenderMock.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage.class));
		Mockito.doThrow(new MailException("error") {}).when(javaMailSenderMock).send(Mockito.any(MimeMessage.class));

		// When
		ResultActions result = mockMvc.perform(get("/api/mail/test").param("address", address).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isInternalServerError());

	}

}



