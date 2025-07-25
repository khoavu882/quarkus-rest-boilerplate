package com.github.kaivu.configuration.security;

import jakarta.enterprise.inject.Default;
import jakarta.ws.rs.core.SecurityContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

@Slf4j
@Getter
@Default
public class AppSecurityContext implements SecurityContext {

    String username;

    public AppSecurityContext(String username) {
        this.username = username;
    }

    @Override
    public Principal getUserPrincipal() {
        return username::toString;
    }

    @Override
    public boolean isUserInRole(String targetRole) {
        return true;
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return null;
    }
}
