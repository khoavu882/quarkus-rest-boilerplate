package com.github.kaivu.infrastructure.middleware;

import com.github.kaivu.core.audit.AuditListener;
import com.github.kaivu.infrastructure.security.AppSecurityContext;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import java.util.Locale;

@Slf4j
@ApplicationScoped
public class DeclaringRoutes {

    private static final String DEFAULT_LANGUAGE = Locale.getDefault().getLanguage();

    @ServerRequestFilter(preMatching = true, nonBlocking = true)
    public Uni<Void> setLanguage(ContainerRequestContext requestContext) {
        return Uni.createFrom().item(requestContext).onItem().transform(ctx -> {
            String contentLang = ctx.getHeaderString(HttpHeaders.CONTENT_LANGUAGE);
            String acceptLang = ctx.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE);
            if (contentLang == null) {
                ctx.getHeaders().putSingle(HttpHeaders.CONTENT_LANGUAGE, DEFAULT_LANGUAGE);
            }
            if (acceptLang == null) {
                ctx.getHeaders().putSingle(HttpHeaders.ACCEPT_LANGUAGE, DEFAULT_LANGUAGE);
            }
            return null;
        });
    }

    @ServerRequestFilter(preMatching = true, nonBlocking = true)
    public Uni<Response> authentication(ContainerRequestContext requestContext) {
        return Uni.createFrom().item(() -> {
            // Add your authentication logic here
            // Example:
            String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (requestContext.getUriInfo().getPath().startsWith("/common")) return null;
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            // Fake authentication for demonstration purposes
            String username = "khoavu882@gmail.com";
            requestContext.setSecurityContext(new AppSecurityContext(username));
            AuditListener.setCurrentUser(username);
            return null; // Continue with the request if authentication passes
        });
    }
}
