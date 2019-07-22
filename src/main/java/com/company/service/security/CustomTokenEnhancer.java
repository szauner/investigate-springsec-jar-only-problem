package com.company.service.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import com.company.service.SecurityConfiguration;
import com.company.service.business.UserManager;
import com.company.service.model.User;

public class CustomTokenEnhancer implements TokenEnhancer {
    private final static Logger log = LogManager.getLogger(CustomTokenEnhancer.class);

    @Autowired
    private UserManager usermanager;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Map<String, Object> additionalInfo = new HashMap<>();

        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            username = (String)principal;
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            username = ((org.springframework.security.core.userdetails.User)principal).getUsername();
        } else {
            username = String.valueOf(principal);
            log.error("Principal is of unknown type. The Enhancer converted the principal to a String, but"
                      + "it is not garantueed that the result will be an E-Mail-Address. Result: {}", username);
        }

        Optional<User> userOpt = usermanager.getUser(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            additionalInfo.put(SecurityConfiguration.ENHANCING_PROPERTY_USER_ID, user.getId());
            additionalInfo.put(SecurityConfiguration.ENHANCING_PROPERTY_APPLICATION_ROLE, user.getRole().toString());
        } else {
            log.error("Access token can't be enriched with user information because no user has been found for principal {}.",
                      username);
        }

        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }
}