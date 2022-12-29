package com.martinia.indigo.domain.services;

import com.martinia.indigo.domain.beans.EmailConfiguration;
import com.martinia.indigo.domain.model.Configuration;
import com.martinia.indigo.ports.in.rest.MailService;
import com.martinia.indigo.ports.out.mail.MailSender;
import com.martinia.indigo.ports.out.mongo.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    MailSender mailSender;

    @Autowired
    ConfigurationRepository configurationRepository;

    private EmailConfiguration getEmailConfig() {
        EmailConfiguration config = new EmailConfiguration();
        config.setHost(configurationRepository.findByKey("smtp.host").map(host -> host.getValue()).orElse(null));
        config.setPort(Integer.parseInt(configurationRepository.findByKey("smtp.port").map(port -> port.getValue()).orElse(String.valueOf(0))));
        config.setUsername(configurationRepository.findByKey("smtp.username").map(username -> username.getValue()).orElse(null));
        config.setPassword(configurationRepository.findByKey("smtp.password").map(password -> password.getValue()).orElse(null));
        config.setEncryption(configurationRepository.findByKey("smtp.encryption").map(encryption -> encryption.getValue()).orElse(null));
        return config;
    }

    @Override
    public void testEmail(String address) {
        boolean ret = mailSender.testEmail(address, getEmailConfig());
        Optional<Configuration> configuration = configurationRepository.findByKey("smtp.status");

        configurationRepository.save(configuration.map(conf -> {
            conf.setValue(ret ? "ok" : "error");
            return conf;
        }).orElse(new Configuration("smtp.status", ret ? "ok" : "error")));
    }

    @Override
    public String mail(String path, String address) {
        return mailSender.mail(path.replace("@_@", "&").replace("@-@", "[").replace("@¡@", "]").replace("@!@", "`"), address, getEmailConfig());
    }

}
