package com.company.service.security;

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import com.company.service.SecurityConfiguration;

public class CustomJwtAccessTokenConverter extends JwtAccessTokenConverter {
    private final static Logger log = LogManager.getLogger(CustomJwtAccessTokenConverter.class);

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        OAuth2Authentication authentication = super.extractAuthentication(map);
        Authentication userAuthentication = authentication.getUserAuthentication();

        if (userAuthentication != null) {
            String email = (String) authentication.getPrincipal();
            String appRoleIdentifier = (String) map.get(SecurityConfiguration.ENHANCING_PROPERTY_APPLICATION_ROLE);

            ApplicationRole appRole;
            try {
                appRole = ApplicationRole.valueOf(appRoleIdentifier);
            } catch (IllegalArgumentException | NullPointerException e) {
                log.error("The given role identifier {} does not match any actual application role!", appRoleIdentifier);
                throw new InsufficientAuthenticationException("Unknown application role!");
            }

            Long userId;
            Object rawId = map.get(SecurityConfiguration.ENHANCING_PROPERTY_USER_ID);

            if (rawId == null) {
                throw new InsufficientAuthenticationException("Authentication information does not contain userId!");
            }

            if (rawId instanceof Long) {
                userId = (Long)rawId;
            }else if (rawId instanceof Integer) {
                userId = ((Integer)rawId).longValue();
            } else if (rawId instanceof String) {
                userId = Long.parseLong((String)rawId);
            } else {
                userId = Long.parseLong(String.valueOf(rawId).toString());
            }

            UserPrincipal extendedPrincipal = new UserPrincipal();
            extendedPrincipal.setEmail(email);
            extendedPrincipal.setUserId(userId);
            extendedPrincipal.setApplicationRole(appRole);

            Collection<? extends GrantedAuthority> authorities = userAuthentication.getAuthorities();
            Object credentials = userAuthentication.getCredentials();

            userAuthentication = new UsernamePasswordAuthenticationToken(extendedPrincipal,
                credentials, authorities);
        }

        return new OAuth2Authentication(authentication.getOAuth2Request(), userAuthentication);
    }
}