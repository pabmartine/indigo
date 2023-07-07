package com.martinia.indigo.mail.application;

import com.martinia.indigo.mail.domain.EmailConfiguration;
import com.martinia.indigo.mail.domain.service.SendMailUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;

@Service
@Slf4j
public class SendMailUseCaseImpl extends BaseMailUseCaseImpl implements SendMailUseCase {

	@Override
	public String mail(final String path, final String address) {
		return send(path.replace("@_@", "&").replace("@-@", "[").replace("@¡@", "]").replace("@!@", "`"), address, getEmailConfig());
	}

	private String send(String path, String address, EmailConfiguration emailConfig) {

		String error = null;

		if (!libraryPath.endsWith(File.separator)) {
			libraryPath += File.separator;
		}

		String basePath = libraryPath + path;

		File file = new File(basePath);
		if (file.exists()) {
			File[] files = file.listFiles();
			Optional<File> epubFile = Arrays.stream(files).filter(f -> f.getName().endsWith(".epub")).findFirst();
			if (epubFile.isPresent()) {
				error = this.sendEmail(epubFile.get().getName(), epubFile.get(), address, emailConfig);
			}
			else {
				error = String.format("Epub file not found in path %s", basePath);
			}

		}
		else {
			error = String.format("Path to %s not exist", basePath);
		}

		return error;
	}

	private String sendEmail(String filename, File f, String address, EmailConfiguration emailConfig) {
		String error = null;
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
		}
		catch (MailException | MessagingException e) {
			error = e.getMessage();
			log.error(error);
		}
		return error;
	}

}
