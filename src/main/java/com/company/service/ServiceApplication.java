package com.company.service;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.company.service.model.JpaMarkerModel;
import com.company.service.persistence.JpaMarkerRepositories;
import com.company.service.util.ApplicationPropertyAccess;

@SpringBootApplication
@EnableJpaRepositories(
    enableDefaultTransactions = false,
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager",
    basePackageClasses = {JpaMarkerRepositories.class}
)
@EnableTransactionManagement
public class ServiceApplication extends SpringBootServletInitializer {
    private final static Logger log = LogManager.getLogger(ServiceApplication.class);

    public static void main(String[] args) {
        new ServiceApplication()
            .configure(new SpringApplicationBuilder(ServiceApplication.class))
            .run(args);
    }

    @Bean(destroyMethod = "close")
    public DataSource dataSource(Environment env) {
        // Use the Tomcat Connection Pool as data source
        org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
        ds.setDriverClassName(env.getRequiredProperty("db.driver"));
        ds.setUrl(env.getRequiredProperty("db.url"));
        ds.setUsername(env.getRequiredProperty("db.username"));
        ds.setPassword(env.getRequiredProperty("db.password"));
        ds.setInitialSize(env.getRequiredProperty("db.pool.initialSize", Integer.class));
        ds.setMaxActive(env.getRequiredProperty("db.pool.maxActive", Integer.class));
        ds.setMinIdle(env.getRequiredProperty("db.pool.minIdle", Integer.class));
        ds.setMaxIdle(env.getRequiredProperty("db.pool.maxIdle", Integer.class));
        ds.setJmxEnabled(env.getRequiredProperty("db.pool.jmxEnabled", Boolean.class));
        return ds;
    }

    @Bean
    public LocalSessionFactoryBean entityManagerFactory(DataSource dataSource, Environment env) {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(dataSource);

        String entityPackage = JpaMarkerModel.class.getPackage().getName();
        log.info("EntityManager will scan for entities in package [{}].", entityPackage);
        factory.setPackagesToScan(entityPackage);

        return factory;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(Environment env) {
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        resourceDatabasePopulator.addScript(new ClassPathResource("data.sql"));

        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource(env));
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
        return dataSourceInitializer;
    }

    @Bean
    public ApplicationPropertyAccess applicationProperties() {
        return new ApplicationPropertyAccess();
    }
}