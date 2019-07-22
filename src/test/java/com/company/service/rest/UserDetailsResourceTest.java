package com.company.service.rest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.company.service.EndToEndTest;
import com.company.service.business.UserManager;
import com.company.service.model.User;
import com.company.service.testdata.UserTestDataProvider;
import com.company.service.util.JsonUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class UserDetailsResourceTest extends EndToEndTest {
    private final static Logger log = LogManager.getLogger(UserDetailsResourceTest.class);

    private final static String BASICAUTH_USERNAME = "frontend";
    private final static String BASICAUTH_PASSWORD = "test";
    private final static String BASE_URL_USER_RESOURCE = "/service/user";

    @Autowired
    private UserManager userManager;

    private HttpHeaders headersStandardUser;
    private HttpHeaders headersAdmin;
    private String authenticationTokenStandardUser;
    private String authenticationTokenAdmin;
    private User testStandardUser;
    private User testAdmin;

    /**
     * Create test user.
     */
    @Before
    public void setup() {
        testStandardUser = UserTestDataProvider.getActiveStandardUser();
        userManager.saveUser(testStandardUser);
        testAdmin = UserTestDataProvider.getAdminUser();
        userManager.saveUser(testAdmin);

        assertThat(testStandardUser.getId(), not(equalTo(testAdmin.getId())));

        String authTokenUrl = UriComponentsBuilder.fromUriString("/oauth/token").toUriString();

        HttpHeaders headersForAuth = new HttpHeaders();
        headersForAuth.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "password");
        body.add("username", testStandardUser.getEmail());
        body.add("password", UserTestDataProvider.getUserPasswordInPlainText());
        HttpEntity<Object> entity = new HttpEntity<Object>(body, headersForAuth);

        ResponseEntity<String> response = restTemplate
            .withBasicAuth(BASICAUTH_USERNAME, BASICAUTH_PASSWORD)
            .postForEntity(authTokenUrl, entity, String.class);

        String authAnswer = response.getBody();
        int indexOfFirstColon = authAnswer.indexOf(":");
        int indexOfFirstComma = authAnswer.indexOf(",");
        authenticationTokenStandardUser = response.getBody().substring(indexOfFirstColon + 2, indexOfFirstComma - 1);

        body.clear();
        body.add("grant_type", "password");
        body.add("username", testAdmin.getEmail());
        body.add("password", UserTestDataProvider.getUserPasswordInPlainText());
        entity = new HttpEntity<Object>(body, headersForAuth);

        response = restTemplate.withBasicAuth(BASICAUTH_USERNAME, BASICAUTH_PASSWORD)
            .postForEntity(authTokenUrl, entity, String.class);

        authAnswer = response.getBody();
        indexOfFirstColon = authAnswer.indexOf(":");
        indexOfFirstComma = authAnswer.indexOf(",");
        authenticationTokenAdmin = response.getBody().substring(indexOfFirstColon + 2, indexOfFirstComma - 1);

        headersStandardUser = new HttpHeaders();
        headersStandardUser.set("Authorization","Bearer "+ authenticationTokenStandardUser);
        headersAdmin = new HttpHeaders();
        headersAdmin.set("Authorization","Bearer "+ authenticationTokenAdmin);
    }

    @Test
    public void testRetrievalOfCustomerDetails() throws JsonParseException, JsonMappingException, IOException {
        String serviceUrl = UriComponentsBuilder.fromUriString(BASE_URL_USER_RESOURCE)
            .queryParam(UserDetailsResource.PARAM_ID, testStandardUser.getId()).toUriString();

        ResponseEntity<String> response = doGetRequest(serviceUrl, headersStandardUser);
        String responsBody = response.getBody();
        log.info("Received the following payload in the reponse: {}", responsBody);

        Map<String,Object> result = JsonUtils.convertJsonToMap(responsBody);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(Long.valueOf(result.get("id").toString()), equalTo(testStandardUser.getId()));
        assertThat(result.get("nickname"), equalTo(testStandardUser.getNickname()));
    }

    @Test
    public void testRetrievalOfAdminDetails() throws JsonParseException, JsonMappingException, IOException {
        String serviceUrl = UriComponentsBuilder.fromUriString(BASE_URL_USER_RESOURCE)
            .queryParam(UserDetailsResource.PARAM_ID, testAdmin.getId()).toUriString();

        ResponseEntity<String> response = doGetRequest(serviceUrl, headersAdmin);
        String responsBody = response.getBody();
        log.info("Received the following payload in the reponse: {}", responsBody);

        Map<String,Object> result = JsonUtils.convertJsonToMap(responsBody);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(Long.valueOf(result.get("id").toString()), equalTo(testAdmin.getId()));
        assertThat(result.get("nickname"), equalTo(testAdmin.getNickname()));
    }

    private ResponseEntity<String> doGetRequest(String url, HttpHeaders head) {
        head.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> httpEntity = new HttpEntity<String>(null, head);
        return this.restTemplate
            .withBasicAuth(BASICAUTH_USERNAME, BASICAUTH_PASSWORD)
            .exchange(url, HttpMethod.GET, httpEntity, String.class);
    }
}