package com.martinia.indigo.services;

import java.io.File;
import java.util.Properties;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.indigo.Configuration;
import com.martinia.indigo.repository.indigo.ConfigurationRepository;
import com.martinia.indigo.repository.indigo.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailService {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Autowired
	private UserRepository userRepository;

	public void init() {
		JavaMailSenderImpl ms = (JavaMailSenderImpl) javaMailSender;
		ms.setHost(configurationRepository.findById("smtp.host")
				.get()
				.getValue());
		ms.setPort(Integer.parseInt(configurationRepository.findById("smtp.port")
				.get()
				.getValue()));
		ms.setUsername(configurationRepository.findById("smtp.username")
				.get()
				.getValue());
		ms.setPassword(configurationRepository.findById("smtp.password")
				.get()
				.getValue());

		Properties props = ms.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");

		if (configurationRepository.findById("smtp.encryption")
				.get()
				.getValue()
				.equals("starttls")) {
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
		} else if (configurationRepository.findById("smtp.encryption")
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

		configurationRepository.save(config);
	}

	public void sendEmail(String filename, File f, int user) throws Exception {

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

}
