package com.martinia.indigo.common.mail.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import lombok.Setter;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SendMailControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private MockMvc mockMvc;

	private JavaMailSender javaMailSender;

	@Value("${book.library.path}")
	@Setter
	protected String libraryPath;

	@BeforeEach
	public void init() {
		javaMailSender = Mockito.mock(JavaMailSenderImpl.class);

		//create db properties
	}

	@Test
	@Disabled
	public void sendMailTest() throws Exception {
		// Given
		String path = "data";
		String address = "test@mail.com";

		if (!libraryPath.endsWith(File.separator)) {
			libraryPath += File.separator;
		}

		String basePath = libraryPath + path;

		Files.createDirectories(Paths.get(basePath));

		FileWriter myWriter = new FileWriter(basePath + "/file");
		myWriter.write("test");
		myWriter.close();

		Mockito.when(javaMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage.class));

		// When
		ResultActions result = mockMvc.perform(
				get("/api/mail/send").param("path", path).param("address", address).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isInternalServerError());

		result.andExpect(jsonPath("$.message", Matchers.is("Epub file not found in path " + basePath)));

		new File(basePath + "/file").delete();
		Files.delete(Paths.get(basePath));

	}

	@Test
	public void sendMailTestNotEpubFile() throws Exception {
		// Given
		String path = "file";
		String address = "test@mail.com";

		if (!libraryPath.endsWith(File.separator)) {
			libraryPath += File.separator;
		}

		String basePath = libraryPath + path;

		FileWriter myWriter = new FileWriter(basePath);
		myWriter.write("test");
		myWriter.close();

		// When
		ResultActions result = mockMvc.perform(
				get("/api/mail/send").param("path", path).param("address", address).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isInternalServerError());

		new File(basePath).delete();
	}

	@Test
	public void sendMailTestFileNotExist() throws Exception {
		// Given
		String path = "file";
		String address = "test@mail.com";

		if (!libraryPath.endsWith(File.separator)) {
			libraryPath += File.separator;
		}

		String basePath = libraryPath + path;

		// When
		ResultActions result = mockMvc.perform(
				get("/api/mail/send").param("path", path).param("address", address).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isInternalServerError());
		result.andExpect(jsonPath("$.message", Matchers.is("Path to " + basePath + " not exist")));

	}

}



