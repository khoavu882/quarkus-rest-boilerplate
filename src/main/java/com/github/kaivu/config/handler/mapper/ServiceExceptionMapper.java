package com.github.kaivu.config.handler.mapper;

import com.github.kaivu.application.exception.NotAcceptableException;
import com.github.kaivu.application.exception.PermissionDeniedException;
import com.github.kaivu.application.exception.UnauthorizedException;
import com.github.kaivu.common.constant.AppHeaderConstant;
import com.github.kaivu.common.constant.ObservabilityConstant;
import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.exception.ObservableServiceException;
import com.github.kaivu.common.exception.ServiceException;
import com.github.kaivu.common.utils.ObservabilityUtil;
import com.github.kaivu.config.handler.ErrorMessage;
import com.github.kaivu.config.handler.ErrorResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 2/12/24
 * Time: 9:04 AM
 */
@Slf4j
@Provider
@ApplicationScoped
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {

    @Context
    ContainerRequestContext requestContext;

    @Inject
    ObservabilityContext observabilityContext;

    @Override
    public Response toResponse(ServiceException ex) {
        // Get error ID from various sources (prioritize correlation ID from context)
        String errorId = getErrorId();

        // Record error in observability span
        ObservabilityUtil.recordError(ex);

        // Enhanced error logging with observability context
        logErrorWithContext(ex, errorId);

        ErrorMessage errorMessage = new ErrorMessage(ex.getEntityName() + "." + ex.getErrorKey(), ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(errorId, errorMessage, ex);

        return buildResponseForExceptionType(ex, errorResponse);
    }

    private String getErrorId() {
        // Prioritize correlation ID from observability context
        String correlationId = observabilityContext.getCorrelationId();
        if (correlationId != null && !correlationId.isEmpty()) {
            return correlationId;
        }

        // Fallback to legacy trace ID header
        String traceId = requestContext.getHeaderString(AppHeaderConstant.TRACE_ID);
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }

        // Fallback to observability context trace ID
        String contextTraceId = observabilityContext.getTraceId();
        if (contextTraceId != null && !contextTraceId.isEmpty()) {
            return contextTraceId;
        }

        return "unknown-error-id";
    }

    private void logErrorWithContext(ServiceException ex, String errorId) {
        if (ex instanceof ObservableServiceException observableEx) {
            // Enhanced logging for observable exceptions
            log.error(
                    "Service error [errorId={}, type={}, entity={}, key={}, context={}]: {}",
                    errorId,
                    ex.getClass().getSimpleName(),
                    ex.getEntityName(),
                    ex.getErrorKey(),
                    observableEx.getContextSummary(),
                    ex.getMessage(),
                    ex);
        } else {
            // Enhanced logging for regular service exceptions
            log.error(
                    "Service error [errorId={}, type={}, entity={}, key={}, context={}]: {}",
                    errorId,
                    ex.getClass().getSimpleName(),
                    ex.getEntityName(),
                    ex.getErrorKey(),
                    observabilityContext.getContextSummary(),
                    ex.getMessage(),
                    ex);
        }
    }

    private ErrorResponse createErrorResponse(String errorId, ErrorMessage errorMessage, ServiceException ex) {
        ErrorResponse response = new ErrorResponse(errorId, errorMessage);

        // Add observability context to error response for debugging
        if (ex instanceof ObservableServiceException observableEx) {
            // Could add additional context fields to ErrorResponse if needed
            // For now, the correlation ID in errorId is sufficient
        }

        return response;
    }

    private Response buildResponseForExceptionType(ServiceException ex, ErrorResponse errorResponse) {
        Response.ResponseBuilder responseBuilder;

        if (ex.getClass().equals(UnauthorizedException.class)) {
            // Remove cookie by re-set value to empty and set expiry to 0 (epoch timestamp)
            NewCookie removeAuthCookie = new NewCookie.Builder("accessToken")
                    .httpOnly(true)
                    .path("/")
                    .value("")
                    .maxAge(0)
                    .build();

            responseBuilder = Response.status(Response.Status.UNAUTHORIZED).cookie(removeAuthCookie);
        } else if (ex.getClass().equals(PermissionDeniedException.class)) {
            responseBuilder = Response.status(Response.Status.FORBIDDEN);
        } else if (ex.getClass().equals(NotAcceptableException.class)) {
            responseBuilder = Response.status(Response.Status.NOT_ACCEPTABLE);
        } else {
            responseBuilder = Response.status(Response.Status.BAD_REQUEST).entity(errorResponse);
        }

        // Add correlation ID header to all error responses
        String correlationId = observabilityContext.getCorrelationId();
        if (correlationId != null && !correlationId.isEmpty()) {
            responseBuilder.header(ObservabilityConstant.CORRELATION_ID_HEADER, correlationId);
        }

        return responseBuilder.build();
    }
}
