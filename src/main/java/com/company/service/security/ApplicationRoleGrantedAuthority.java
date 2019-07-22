package com.company.service.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

public final class ApplicationRoleGrantedAuthority implements GrantedAuthority {
    private static final long serialVersionUID = 1L;

    private final String role;

    public ApplicationRoleGrantedAuthority(String role) {
        Assert.hasText(role, "A granted authority textual representation is required");
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return role;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ApplicationRoleGrantedAuthority) {
            return role.equals(((ApplicationRoleGrantedAuthority) obj).role);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.role.hashCode();
    }

    @Override
    public String toString() {
        return this.role;
    }
}