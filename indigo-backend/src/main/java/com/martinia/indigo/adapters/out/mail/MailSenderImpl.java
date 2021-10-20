package com.martinia.indigo.adapters.out.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

import com.martinia.indigo.domain.beans.EmailConfiguration;
import com.martinia.indigo.ports.out.mail.MailSender;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailSenderImpl implements MailSender {

	@Autowired
	private JavaMailSender javaMailSender;

	@Value("${book.library.path}")
	private String libraryPath;

	private void init(EmailConfiguration emailConfig) {

		JavaMailSenderImpl ms = (JavaMailSenderImpl) javaMailSender;
		ms.setHost(emailConfig.getHost());
		ms.setPort(emailConfig.getPort());
		ms.setUsername(emailConfig.getUsername());
		ms.setPassword(emailConfig.getPasswor());

		Properties props = ms.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");

		String encryption = emailConfig.getEncryption();

		if (encryption.equals("starttls")) {
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
		} else if (encryption.equals("ssl/tls")) {
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}

	}

	@Override
	public String mail(String path, String address, EmailConfiguration emailConfig) {

		String error = null;

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

					String cmd = emailConfig.getKindlegen() + " " + destination.getPath();
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
					this.sendEmail(mobi.getName(), mobi, address, emailConfig);
				} catch (Exception e) {
					error = e.getMessage();
					log.error(error);
				}
			}

		}
		return error;
	}

	private void sendEmail(String filename, File f, String address, EmailConfiguration emailConfig) throws Exception {

		try {
			init(emailConfig);

			MimeMessage message = javaMailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom("no-reply@indigo.com");
			helper.setTo(address);
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

	@Override
	public boolean testEmail(String address, EmailConfiguration emailConfig) {
		boolean ret = false;
		try {

			init(emailConfig);

			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(address);
			message.setFrom("no-Reply@indigo.com");
			message.setSubject("Test mail");
			message.setText("This is a test mail");

			javaMailSender.send(message);

			ret = true;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return ret;
	}

}
