package com.company.service.security;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.company.service.business.UserDetailsServiceImpl;

/**
 * Principal class that stores authentication and authorisation information.
 *
 * @author Stefan Zauner
 */
public class UserPrincipal {
    private String email;
    private Long userId;
    private ApplicationRole applicationRole;
    private List<String> permissions;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ApplicationRole getApplicationRole() {
        return applicationRole;
    }

    public void setApplicationRole(ApplicationRole applicationRole) {
        this.applicationRole = applicationRole;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    /**
     * The toString-method returns the e-mail of the user this principal is associated with. This is necessary
     * because Spring internally needs a String-principal to load the user from the persistent storage, see
     * {@link UserDetailsService} and {@link UserDetailsServiceImpl}.
     */
    @Override
    public String toString() {
        return email;
    }
}
