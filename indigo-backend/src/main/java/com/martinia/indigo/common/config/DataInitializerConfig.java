package com.martinia.indigo.common.config;

import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.user.domain.model.RolesEnum;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

@Slf4j
@Component
		//@Profile("!test")
class DataInitializerConfig {

	@Resource
	private ConfigurationRepository configurationRepository;

	@Autowired
	private UserRepository userRepository;

	@Value("${data.initializer.user.admin.username}")
	private String username;

	@Value("${data.initializer.user.admin.password}")
	private String password;

	@Value("${data.initializer.books.recommendations}")
	private String recommendations;

	@PostConstruct
	private void init() throws ScriptException, SQLException {

		if (userRepository.findByUsername(username).isEmpty()) {
			UserMongoEntity user = UserMongoEntity.builder()
					.username(username)
					.password(password)
					.role(RolesEnum.ADMIN.name())
					.language(Locale.ENGLISH.getLanguage())
					.languageBooks(Arrays.asList("spa", "eng"))
					.build();
			userRepository.save(user);
		}

		if (configurationRepository.findByKey("books.recommendations").isEmpty()) {
			configurationRepository.save(ConfigurationMongoEntity.builder().key("books.recommendations").value(recommendations).build());
		}

	}

}
