package com.company.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@SpringBootTest(classes=TestApplicationContext.class, webEnvironment=WebEnvironment.RANDOM_PORT)
public abstract class EndToEndTest extends AbstractJUnit4SpringContextTests {
    // provide the random port by letting spring inject it into a member field
    @LocalServerPort
    protected int randomServerPort;

    // provide means to access REST endpoints for every integration test
    @Autowired
    protected TestRestTemplate restTemplate;
}