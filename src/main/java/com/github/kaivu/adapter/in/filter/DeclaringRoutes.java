package com.github.kaivu.adapter.in.filter;

import com.github.kaivu.common.constant.AppHeaderConstant;
import com.github.kaivu.configuration.security.AppSecurityContext;
import com.github.kaivu.domain.audit.AuditListener;
import io.opentelemetry.api.trace.Span;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import java.util.Locale;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class DeclaringRoutes {

    private static final String DEFAULT_LANGUAGE = Locale.getDefault().getLanguage();

    @ServerRequestFilter(preMatching = true, nonBlocking = true)
    public Uni<Void> setDefaultHeadersValue(ContainerRequestContext requestContext) {

        Map<String, String> DEFAULT_HEADERS = Map.of(
                HttpHeaders.ACCEPT_LANGUAGE, DEFAULT_LANGUAGE,
                HttpHeaders.CONTENT_LANGUAGE, DEFAULT_LANGUAGE,
                AppHeaderConstant.TRACE_ID, Span.current().getSpanContext().getTraceId()
                // Add more headers as needed
                );

        return Uni.createFrom().item(requestContext).onItem().transform(ctx -> {
            DEFAULT_HEADERS.forEach((key, valueSupplier) -> {
                if (ctx.getHeaderString(key) == null) {
                    ctx.getHeaders().putSingle(key, valueSupplier);
                }
            });
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
