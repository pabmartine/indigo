package com.martinia.indigo.config;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@PropertySource({"classpath:application.properties"})
@EnableJpaRepositories(
  basePackages = "com.martinia.indigo.repository.indigo", 
  entityManagerFactoryRef = "indigoEntityManager", 
  transactionManagerRef = "indigoTransactionManager")
public class PersistenceIndigoConfiguration {
    
	@Bean(name = "indigoDataSource")
    @ConfigurationProperties(prefix="spring.datasource-indigo")
    public DataSource indigoDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean indigoEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(indigoDataSource());
        em.setPackagesToScan(new String[] {"com.martinia.indigo.model.indigo"});

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<String, Object>();
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "indigoTransactionManager")
    public PlatformTransactionManager indigoTransactionManager() {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(indigoEntityManager().getObject());
        return tm;
    }	
}