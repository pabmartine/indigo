package com.martinia.indigo.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.indigo.Configuration;
import com.martinia.indigo.repository.indigo.UserRepository;
import com.martinia.indigo.services.indigo.ConfigurationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailService {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private UserRepository userRepository;

	@Value("${book.library.path}")
	private String libraryPath;

	private void init() {
		JavaMailSenderImpl ms = (JavaMailSenderImpl) javaMailSender;
		ms.setHost(configurationService.findById("smtp.host")
				.get()
				.getValue());
		ms.setPort(Integer.parseInt(configurationService.findById("smtp.port")
				.get()
				.getValue()));
		ms.setUsername(configurationService.findById("smtp.username")
				.get()
				.getValue());
		ms.setPassword(configurationService.findById("smtp.password")
				.get()
				.getValue());

		Properties props = ms.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");

		if (configurationService.findById("smtp.encryption")
				.get()
				.getValue()
				.equals("starttls")) {
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
		} else if (configurationService.findById("smtp.encryption")
				.get()
				.getValue()
				.equals("ssl/tls")) {
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}

	}

	public void testEmail(int user) {

		Configuration config = new Configuration("smtp.status", "unknown");
		try {
			init();

			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(userRepository.findById(user)
					.get()
					.getKindle());
			message.setFrom("no-Reply@indigo.com");
			message.setSubject("Test mail");
			message.setText("This is a test mail");

			javaMailSender.send(message);

			config.setValue("ok");
		} catch (Exception e) {
			log.error(e.getMessage());
			config.setValue("error");
		}

		configurationService.save(Arrays.asList(config));
	}

	private void sendEmail(String filename, File f, int user) throws Exception {

		try {
			init();

			MimeMessage message = javaMailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom("no-reply@indigo.com");
			helper.setTo(userRepository.findById(user)
					.get()
					.getKindle());
			helper.setSubject("INDIGO");
			helper.setText(filename + "sent to Kindle");

			FileSystemResource file = new FileSystemResource(f);
			helper.addAttachment(filename, file);

			javaMailSender.send(message);

		} finally {
			// delete .mobi after send
			f.delete();
		}

	}

	public String mail(String path, int user) {

		String error = null;

		String kindlegenPath = configurationService.findById("kindlegen.path")
				.get()
				.getValue();

		if (!libraryPath.endsWith(File.separator))
			libraryPath += File.separator;

		String basePath = libraryPath + path;

		File file = new File(basePath);
		if (file.exists()) {
			File[] files = file.listFiles();
			File epub = null;
			File mobi = null;
			for (File f : files) {
				if (f.getName()
						.endsWith(".epub"))
					epub = f;
				else if (f.getName()
						.endsWith(".mobi"))
					mobi = f;
			}

			if (mobi == null) {
				try {

					String name = epub.getName()
							.substring(0, epub.getName()
									.indexOf("."));
					String newName = Base64.getEncoder()
							.encodeToString(name.getBytes()) + ".epub";

					File destination = new File(libraryPath + newName);

					Files.copy(epub.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

					String cmd = kindlegenPath + " " + destination.getPath();
					log.info(cmd);
					Process process = Runtime.getRuntime()
							.exec(cmd);
					InputStream is = null;
					BufferedReader br = null;
					InputStreamReader isr = null;
					String textLine = "";
					is = process.getInputStream();
					isr = new InputStreamReader(is);
					br = new BufferedReader(isr);
					while ((textLine = br.readLine()) != null) {
						if (0 != textLine.length()) {
							log.info(textLine);
						}
					}

					mobi = new File(destination.getPath()
							.replace(".epub", ".mobi"));

					destination.delete();

				} catch (IOException e) {
					error = e.getMessage();
					log.error(error);
				}
			}

			if (mobi != null) {
				try {
					this.sendEmail(mobi.getName(), mobi, user);
				} catch (Exception e) {
					error = e.getMessage();
					log.error(error);
				}
			}

		}
		return error;
	}

}
