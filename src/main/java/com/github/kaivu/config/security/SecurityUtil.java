package com.github.kaivu.config.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
public class SecurityUtil {

    private SecurityUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getUsername(ContainerRequestContext requestContext) {
        AppSecurityContext appSecurityContext = (AppSecurityContext) requestContext.getSecurityContext();
        return appSecurityContext.getUsername();
    }
}
