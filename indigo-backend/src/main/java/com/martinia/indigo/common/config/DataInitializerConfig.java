package com.martinia.indigo.common.config;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import javax.annotation.PostConstruct;

import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.stereotype.Component;
import com.martinia.indigo.user.domain.model.RolesEnum;
import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.repository.ConfigurationRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
class DataInitializerConfig {

  @Autowired
  private ConfigurationRepository configurationRepository;

  @Autowired
  private UserRepository userRepository;

  @Value("${data.initializer.user.admin.username}")
  private String username;

  @Value("${data.initializer.user.admin.password}")
  private String password;

  @Value("${data.initializer.metadata.pull}")
  private String pull;

  @Value("${data.initializer.books.recommendations}")
  private String recommendations;

  @PostConstruct
  private void init() throws ScriptException, SQLException {

    // Insert admin user
    log.info("Initializating users...");
    if (userRepository.findByUsername(username).isEmpty()) {
      User user = new User(username, password,
          RolesEnum.ADMIN.name(), Locale.ENGLISH.getLanguage(), Arrays.asList("spa", "eng"));
      userRepository.save(user);
    }

    log.info("Initializating config...");
    if (configurationRepository.findByKey("metadata.pull").isEmpty()) {
      configurationRepository.save(new Configuration("metadata.pull", pull));
    }

    if (configurationRepository.findByKey("books.recommendations").isEmpty()) {
      configurationRepository.save(new Configuration("books.recommendations", recommendations));
    }

  }

}
