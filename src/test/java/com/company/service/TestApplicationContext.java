package com.company.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = "classpath:application.properties")
public class TestApplicationContext extends ServiceApplication {
    private final static Logger log = LogManager.getLogger(TestApplicationContext.class);

    @Bean
    public TransactionalTestHelper transactionalTestHelper() {
        return new TransactionalTestHelperImpl();
    }
}
