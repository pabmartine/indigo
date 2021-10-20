package com.martinia.indigo.domain.config;

import java.sql.SQLException;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.stereotype.Component;

import com.martinia.indigo.domain.enums.RolesEnum;
import com.martinia.indigo.domain.model.Configuration;
import com.martinia.indigo.domain.model.User;
import com.martinia.indigo.ports.out.mongo.ConfigurationRepository;
import com.martinia.indigo.ports.out.mongo.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
class DataInitializerConfig {

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Autowired
	private UserRepository userRepository;

	@PostConstruct
	private void init() throws ScriptException, SQLException {

		// Insert admin user
		log.info("Initializating users...");
		if (userRepository.findByUsername("admin") == null) {
			User user = new User("admin", "$2a$10$U47NDeBbqVmBHQsYXh8HI.IpxGiopQ0cWgXTkwU/AwFmGRf5l9lfq",
					RolesEnum.ADMIN.name(), Locale.ENGLISH.getLanguage());
			userRepository.save(user);
		}

		log.info("Initializating config...");
		if (configurationRepository.findByKey("metadata.pull") == null) {
			configurationRepository.save(new Configuration("metadata.pull", "5000"));
		}

		// TODO: Estas configuraciones son solo para pruebas, luego borrarlas
		if (configurationRepository.findByKey("kindlegen.path") == null) {
			configurationRepository.save(
					new Configuration("kindlegen.path", "C:/Users/Pablo/Downloads/kindlegen_win32_v2_9/kindlegen.exe"));
		}

		if (configurationRepository.findByKey("goodreads.key") == null) {
			configurationRepository.save(new Configuration("goodreads.key", "usnvwo0d4PBtqDygTvVUcQ"));
		}

		if (configurationRepository.findByKey("smtp.host") == null) {
			configurationRepository.save(new Configuration("smtp.host", "smtp.gmail.com"));
		}
		if (configurationRepository.findByKey("smtp.port") == null) {
			configurationRepository.save(new Configuration("smtp.port", "587"));
		}
		if (configurationRepository.findByKey("smtp.encryption") == null) {
			configurationRepository.save(new Configuration("smtp.encryption", "starttls"));
		}
		if (configurationRepository.findByKey("smtp.username") == null) {
			configurationRepository.save(new Configuration("smtp.username", "pabmartine@gmail.com"));
		}
		if (configurationRepository.findByKey("smtp.password") == null) {
			configurationRepository.save(new Configuration("smtp.password", "escribano82"));
		}
		if (configurationRepository.findByKey("smtp.status") == null) {
			configurationRepository.save(new Configuration("smtp.status", "ok"));
		}
		if (configurationRepository.findByKey("books.recommendations") == null) {
			configurationRepository.save(new Configuration("books.recommendations", "10"));
		}

		if (configurationRepository.findByKey("books.recommendations2") == null) {
			configurationRepository.save(new Configuration("books.recommendations2", "24"));
		}
	}

}