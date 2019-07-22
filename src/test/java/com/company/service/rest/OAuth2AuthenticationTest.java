package com.company.service.rest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.company.service.EndToEndTest;
import com.company.service.SecurityConfiguration;
import com.company.service.TransactionalTestHelper;
import com.company.service.business.UserManager;
import com.company.service.model.User;
import com.company.service.testdata.UserTestDataProvider;
import com.company.service.util.JsonUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class OAuth2AuthenticationTest extends EndToEndTest {
    private final static String BASICAUTH_USERNAME = "frontend";
    private final static String BASICAUTH_PASSWORD = "test";


    private static String serviceUrl;
    private static HttpHeaders headers;

    @Autowired
    private TransactionalTestHelper transactionalTestHelper;
    @Autowired
    private UserManager userManager;

    private User testUser;
    private User lockedTestUser;

    @BeforeClass
    public static void init() {
        serviceUrl = UriComponentsBuilder.fromUriString("/oauth/token").toUriString();
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Before
    public void setup() {
        testUser = UserTestDataProvider.getActiveStandardUser();
        userManager.saveUser(testUser);
        lockedTestUser = UserTestDataProvider.getLockedAdminUser();
        userManager.saveUser(lockedTestUser);
    }

    @Test
    public void testSecurenessOfAuthorisationEndpoint() {
        String authorizeUrl = UriComponentsBuilder.fromUriString("/oauth/authorize").toUriString();
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        ResponseEntity<String> response = restTemplate.postForEntity(authorizeUrl, entity, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testAuthenticationAsLockedUser() {
        ResponseEntity<String> response = sendPostToAuthenticationService(lockedTestUser.getEmail(),
            UserTestDataProvider.getUserPasswordInPlainText());

        assertThat(response.getBody(), containsString("User account is locked"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testSuccessfulAuthentication() throws JsonParseException, JsonMappingException, IOException {
        ResponseEntity<String> response = sendPostToAuthenticationService(testUser.getEmail(),
            UserTestDataProvider.getUserPasswordInPlainText());

        assertThat(response.getBody(), containsString("access_token"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String,Object> payload = JsonUtils.convertJsonToMap(response.getBody());

        assertThat(payload.get(SecurityConfiguration.ENHANCING_PROPERTY_USER_ID),
                   equalTo(Integer.parseInt(testUser.getId().toString())));
        assertThat(payload.get(SecurityConfiguration.ENHANCING_PROPERTY_APPLICATION_ROLE),
                   equalTo(testUser.getRole().toString()));
        assertThat(payload.get("token_type"), equalTo("bearer"));
        assertThat(payload.get("access_token").toString(), not(isEmptyOrNullString()));
        assertThat(payload.get("refresh_token").toString(), not(isEmptyOrNullString()));
    }

    @After
    public void cleanup() {
        transactionalTestHelper.deleteUser(testUser);
        transactionalTestHelper.deleteUser(lockedTestUser);
    }

    /*
     * Helper method for sending requests to the authentication endpoint.
     */
    private ResponseEntity<String> sendPostToAuthenticationService(String email, String password) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "password");

        if (email != null) {
            body.add("username", email);
        }

        if (password != null) {
            body.add("password", password);
        }

        HttpEntity<Object> entity = new HttpEntity<Object>(body, headers);

        return restTemplate
            .withBasicAuth(BASICAUTH_USERNAME, BASICAUTH_PASSWORD)
            .postForEntity(serviceUrl, entity, String.class);
    }
}