package com.martinia.indigo.config;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import com.martinia.indigo.enums.RolesEnum;
import com.martinia.indigo.model.indigo.Configuration;
import com.martinia.indigo.model.indigo.User;
import com.martinia.indigo.repository.indigo.ConfigurationRepository;
import com.martinia.indigo.repository.indigo.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
class DataInitializer {

	@Autowired
	@Qualifier("indigoDataSource")
	private DataSource dataSource;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ConfigurationRepository configurationRepository;

	@PostConstruct
	private void init() throws ScriptException, SQLException {

		ScriptUtils.executeSqlScript(dataSource.getConnection(),
				new EncodedResource(new ClassPathResource("schema.sql"), StandardCharsets.UTF_8));

		// Insert admin user
		log.info("Initializating users...");
		if (userRepository.findByUsername("admin") == null) {
			User user = new User("admin", "$2a$10$U47NDeBbqVmBHQsYXh8HI.IpxGiopQ0cWgXTkwU/AwFmGRf5l9lfq", null,
					RolesEnum.ADMIN.name(), Locale.ENGLISH.getLanguage());
			user.setShowRandomBooks(false);
			userRepository.save(user);
		}

		log.info("Initializating config...");
		if (!configurationRepository.findById("metadata.pull")
				.isPresent()) {
			Configuration config = new Configuration("metadata.pull", "5000");
			configurationRepository.save(config);
		}

		// TODO: Estas configuraciones son solo para pruebas, luego borrarlas

		if (!configurationRepository.findById("kindlegen.path")
				.isPresent()) {
			Configuration config = new Configuration("kindlegen.path",
					"C:/Users/Pablo/Downloads/kindlegen_win32_v2_9/kindlegen.exe");
			configurationRepository.save(config);
		}

		if (!configurationRepository.findById("goodreads.key")
				.isPresent()) {
			Configuration config = new Configuration("goodreads.key", "usnvwo0d4PBtqDygTvVUcQ");
			configurationRepository.save(config);
		}

		if (!configurationRepository.findById("smtp.host")
				.isPresent()) {
			Configuration config = new Configuration("smtp.host", "smtp.gmail.com");
			configurationRepository.save(config);
		}
		if (!configurationRepository.findById("smtp.port")
				.isPresent()) {
			Configuration config = new Configuration("smtp.port", "587");
			configurationRepository.save(config);
		}
		if (!configurationRepository.findById("smtp.encryption")
				.isPresent()) {
			Configuration config = new Configuration("smtp.encryption", "starttls");
			configurationRepository.save(config);
		}
		if (!configurationRepository.findById("smtp.username")
				.isPresent()) {
			Configuration config = new Configuration("smtp.username", "pabmartine@gmail.com");
			configurationRepository.save(config);
		}
		if (!configurationRepository.findById("smtp.password")
				.isPresent()) {
			Configuration config = new Configuration("smtp.password", "escribano82");
			configurationRepository.save(config);
		}
		if (!configurationRepository.findById("smtp.status")
				.isPresent()) {
			Configuration config = new Configuration("smtp.status", "ok");
			configurationRepository.save(config);
		}
		if (!configurationRepository.findById("books.recommendations")
				.isPresent()) {
			Configuration config = new Configuration("books.recommendations", "10");
			configurationRepository.save(config);
		}
	}

}