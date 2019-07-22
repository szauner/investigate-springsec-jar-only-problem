package com.company.service.security;

import org.springframework.security.core.Authentication;

public interface AuthenticationFacade {
    public Authentication getAuthentication();
}