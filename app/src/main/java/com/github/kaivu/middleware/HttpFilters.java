package com.github.kaivu.middleware;

import com.github.kaivu.config.ConfigsProvider;
import com.github.kaivu.models.audit.AuditListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Provider
@ApplicationScoped
public class HttpFilters implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String MANAGEMENT_PREFIX_PATH = "/q/";
    private static final String GZIP_ENCODING = "gzip";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (Boolean.TRUE.equals(ConfigsProvider.ENABLE_AUTH_LOGGING)) {
            // Add authentication logging if needed
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        logHttpRequest(requestContext, responseContext);
        handleCompression(requestContext, responseContext);
        AuditListener.clearCurrentUser();
    }

    private void logHttpRequest(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String path = requestContext.getUriInfo().getPath();
        if (!path.contains(MANAGEMENT_PREFIX_PATH)) {
            log.info(
                    "(HTTP) method: {}, host: {}, path: {}, status: {}, locate: {}",
                    requestContext.getMethod().toUpperCase(Locale.ROOT),
                    requestContext.getUriInfo().getBaseUri().getHost(),
                    path,
                    responseContext.getStatus(),
                    requestContext.getLanguage());
        }
    }

    private void handleCompression(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        if (Boolean.FALSE.equals(ConfigsProvider.ENABLE_COMPRESSION)) {
            return;
        }

        String encoding = requestContext.getHeaderString(HttpHeaders.ACCEPT_ENCODING);
        if (encoding != null && encoding.contains(GZIP_ENCODING)) {
            responseContext.getHeaders().put(HttpHeaders.CONTENT_ENCODING, List.of(GZIP_ENCODING));
            OutputStream outputStream = responseContext.getEntityStream();
            responseContext.setEntityStream(new GZIPOutputStream(outputStream));
        }
    }
}
