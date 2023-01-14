package com.martinia.indigo.adapters.out.mail;

import com.martinia.indigo.domain.beans.EmailConfiguration;
import com.martinia.indigo.ports.out.mail.MailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

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
        ms.setPassword(emailConfig.getPassword());

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
            Optional<File> epubFile = Arrays.stream(files).filter(f -> f.getName().endsWith(".epub")).findFirst();
            if (epubFile.isPresent())
                error = this.sendEmail(epubFile.get().getName(), epubFile.get(), address, emailConfig);
            else
                error = String.format("Epub file not found in path %s", basePath);

        } else {
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
        } catch (MailException | MessagingException e) {
            error = e.getMessage();
            log.error(error);
        }
        return error;
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
        } catch (MailException e) {
            log.error(e.getMessage());
        }
        return ret;
    }

}
