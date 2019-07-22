package com.company.service.security;

public interface AuthManager {

    public boolean checkAuthenticationStatus(Long userId);

    public Long getAuthenticatedUsersId();

    public UserPrincipal getAuthenticatedPrincipal(boolean allowAnonymous) throws NotAuthenticatedException;
}
